package com.reboot.identity.service.impl;

import com.reboot.identity.model.AdGroupRoleMapping;
import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.Role;
import com.reboot.identity.model.User;
import com.reboot.identity.repository.AdGroupRoleMappingRepository;
import com.reboot.identity.repository.RoleRepository;
import com.reboot.identity.repository.UserRepository;
import com.reboot.identity.service.DirectoryAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ActiveDirectoryLdapsAuthenticator implements DirectoryAuthenticator {

    @Value("${ad.enabled:false}")
    private boolean adEnabled;

    @Value("${ad.ldap.url}")
    private String ldapUrl;

    @Value("${ad.base-dn}")
    private String baseDn;

    @Value("${ad.bind-dn}")
    private String bindDn;

    @Value("${ad.bind-password}")
    private String bindPassword;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdGroupRoleMappingRepository adGroupRoleMappingRepository;

    public ActiveDirectoryLdapsAuthenticator(UserRepository userRepository,
                                             RoleRepository roleRepository,
                                             AdGroupRoleMappingRepository adGroupRoleMappingRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.adGroupRoleMappingRepository = adGroupRoleMappingRepository;
    }

    private LdapTemplate createLdapTemplate(String userDn, String password) {
        if (!StringUtils.startsWithIgnoreCase(ldapUrl, "ldaps://")) {
            throw new IllegalStateException("Active Directory requires an LDAPS URL.");
        }

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase("");
        contextSource.setUserDn(userDn);
        contextSource.setPassword(password);
        contextSource.afterPropertiesSet();
        return new LdapTemplate(contextSource);
    }

    @Override
    @Transactional
    public User authenticateAndSynchronize(String usernameOrUpn, String password) {
        if (!adEnabled) {
            log.warn("Active Directory authentication attempted while AD is disabled.");
            throw new IllegalStateException("Active Directory authentication is currently disabled.");
        }
        if (!StringUtils.hasText(usernameOrUpn) || !StringUtils.hasText(password)) {
            log.warn("Active Directory authentication failed because the username or password was blank.");
            throw new BadCredentialsException("Invalid Active Directory credentials.");
        }

        try {
            LdapTemplate searchTemplate = createLdapTemplate(bindDn, bindPassword);
            String escapedUsername = LdapEncoder.filterEncode(usernameOrUpn);
            String filter = "(&(objectClass=user)(|(userPrincipalName=" + escapedUsername
                    + ")(sAMAccountName=" + escapedUsername + ")))";
            ContextMapper<DirContextOperations> contextMapper =
                    context -> (DirContextOperations) context;
            List<DirContextOperations> results = searchTemplate.search(baseDn, filter, contextMapper);

            if (results.isEmpty()) {
                log.warn("Active Directory authentication failed: no matching user was found.");
                throw new BadCredentialsException("Invalid Active Directory credentials.");
            }

            DirContextOperations userContext = results.get(0);
            String userDn = userContext.getNameInNamespace();
            try {
                createLdapTemplate(userDn, password)
                        .executeReadOnly(context -> context.getEnvironment());
            } catch (AuthenticationException exception) {
                log.warn("Active Directory authentication failed because the supplied credentials were rejected.");
                throw new BadCredentialsException("Invalid Active Directory credentials.");
            }

            User synchronizedUser = synchronizeUser(userContext);
            log.info("Authenticated and synchronized Active Directory user [{}] with {} mapped roles.",
                    synchronizedUser.getUsername(), synchronizedUser.getRoles().size());
            return synchronizedUser;
        } catch (BadCredentialsException | IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Technical failure during LDAPS authentication or synchronization ({}).",
                    exception.getClass().getSimpleName());
            throw new IllegalStateException("Active Directory service technical failure.", exception);
        }
    }

    private User synchronizeUser(DirContextOperations userContext) {
        String email = userContext.getStringAttribute("mail");
        String displayName = userContext.getStringAttribute("displayName");
        if (!StringUtils.hasText(displayName)) {
            displayName = userContext.getStringAttribute("cn");
        }
        String username = userContext.getStringAttribute("sAMAccountName");

        if (!StringUtils.hasText(email) || !StringUtils.hasText(displayName) || !StringUtils.hasText(username)) {
            throw new IllegalStateException("Active Directory user is missing required profile attributes.");
        }

        String[] memberOf = userContext.getStringAttributes("memberOf");
        Set<Role> assignedRoles = new HashSet<>();
        if (memberOf != null) {
            for (String groupDn : memberOf) {
                Optional<AdGroupRoleMapping> mapping =
                        adGroupRoleMappingRepository.findByAdGroupDn(groupDn);
                mapping.map(AdGroupRoleMapping::getRole).ifPresent(assignedRoles::add);
            }
        }

        if (assignedRoles.isEmpty()) {
            roleRepository.findByName("USER")
                    .or(() -> roleRepository.findByName("ROLE_USER"))
                    .ifPresent(assignedRoles::add);
        }

        Optional<User> existingUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(email));
        User user = existingUser.orElseGet(User::new);
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(displayName);
        user.setAuthProvider(AuthProvider.ACTIVE_DIRECTORY);
        user.setPasswordHash(null);
        user.setRoles(assignedRoles);

        User savedUser = userRepository.save(user);
        log.info("Synchronized Active Directory profile and group mappings for user [{}].", username);
        return savedUser;
    }

    @Override
    public boolean testConnectivity() {
        if (!adEnabled) {
            log.warn("AD LDAPS connectivity test skipped because AD is disabled.");
            return false;
        }

        try {
            createLdapTemplate(bindDn, bindPassword)
                    .executeReadOnly(context -> context.getEnvironment());
            log.info("AD LDAPS connectivity test succeeded.");
            return true;
        } catch (Exception exception) {
            log.error("AD LDAPS connectivity test failed ({}).",
                    exception.getClass().getSimpleName());
            return false;
        }
    }
}
