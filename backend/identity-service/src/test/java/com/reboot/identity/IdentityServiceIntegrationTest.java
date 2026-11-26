package com.reboot.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.SelfSignedCertificateGenerator;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.util.ObjectPair;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import com.reboot.identity.model.AdGroupRoleMapping;
import com.reboot.identity.model.Role;
import com.reboot.identity.model.User;
import com.reboot.identity.repository.AdGroupRoleMappingRepository;
import com.reboot.identity.repository.PasswordResetTokenRepository;
import com.reboot.identity.repository.RefreshSessionRepository;
import com.reboot.identity.repository.RoleRepository;
import com.reboot.identity.repository.UserRepository;
import com.reboot.identity.service.DirectoryAuthenticator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import javax.net.ssl.SSLContext;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IdentityServiceIntegrationTest {

    private static final String BASE_DN = "dc=reboot,dc=com";
    private static final String SERVICE_DN = "cn=service," + BASE_DN;
    private static final String SERVICE_PASSWORD = "service-bind-password";
    private static final String USER_DN = "cn=Julian Soto,ou=Users," + BASE_DN;
    private static final String USER_PASSWORD = "directory-password";
    private static final String GROUP_DN = "cn=REBOOT,ou=Groups," + BASE_DN;

    private static InMemoryDirectoryServer directoryServer;
    private static File keyStoreFile;
    private static String previousEndpointIdentification;
    private static SSLContext previousSslContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DirectoryAuthenticator directoryAuthenticator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AdGroupRoleMappingRepository mappingRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RefreshSessionRepository refreshSessionRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        startDirectoryServer();
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:identity-it;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("ad.enabled", () -> "true");
        registry.add("ad.ldap.url", () -> "ldaps://localhost:" + directoryServer.getListenPort());
        registry.add("ad.base-dn", () -> BASE_DN);
        registry.add("ad.bind-dn", () -> SERVICE_DN);
        registry.add("ad.bind-password", () -> SERVICE_PASSWORD);
        registry.add("jwt.secret", () -> "integration-test-secret-longer-than-thirty-two-bytes");
        registry.add("app.mail.from", () -> "noreply@example.test");
    }

    @BeforeEach
    void setUpDatabase() {
        mappingRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        refreshSessionRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role rebootRole = new Role();
        rebootRole.setName("REBOOT");
        rebootRole.setDescription("Directory-mapped test role");
        rebootRole = roleRepository.save(rebootRole);

        AdGroupRoleMapping mapping = new AdGroupRoleMapping();
        mapping.setAdGroupDn(GROUP_DN);
        mapping.setRole(rebootRole);
        mappingRepository.save(mapping);
    }

    @Test
    void connectsAuthenticatesMapsGroupsIssuesJwtAndAuthorizesCurrentProfile() throws Exception {
        assertTrue(directoryAuthenticator.testConnectivity());

        String loginJson = """
                {
                  "usernameOrEmail": "jsoto",
                  "password": "directory-password",
                  "clientType": "WEB",
                  "deviceInfo": "integration-test"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/identity/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresIn").isNumber())
                .andExpect(jsonPath("$.data.user.authProvider").value("ACTIVE_DIRECTORY"))
                .andExpect(jsonPath("$.data.user.roles[0]").value("REBOOT"))
                .andReturn();

        JsonNode response = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = response.path("data").path("accessToken").asText();
        User synchronizedUser = userRepository.findByUsername("jsoto").orElseThrow();
        assertNull(synchronizedUser.getPasswordHash());
        assertEquals("REBOOT", synchronizedUser.getRoles().iterator().next().getName());

        mockMvc.perform(get("/api/v1/identity/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(synchronizedUser.getId().toString()))
                .andExpect(jsonPath("$.data.username").value("jsoto"))
                .andExpect(jsonPath("$.data.roles[0]").value("REBOOT"))
                .andExpect(jsonPath("$.meta.requestId").isNotEmpty());
    }

    @Test
    void protectsCurrentProfileWith401AndAdminRouteWith403() throws Exception {
        mockMvc.perform(get("/api/v1/identity/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.requestId").isNotEmpty());

        String loginJson = """
                {"usernameOrEmail":"jsoto","password":"directory-password","clientType":"WEB"}
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/v1/identity/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/identity/users/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @AfterAll
    static void stopDirectoryServer() {
        if (directoryServer != null) {
            directoryServer.shutDown(true);
        }
        if (keyStoreFile != null) {
            keyStoreFile.delete();
        }
        restoreSystemProperty("com.sun.jndi.ldap.object.disableEndpointIdentification",
                previousEndpointIdentification);
        if (previousSslContext != null) {
            try {
                SSLContext.setDefault(previousSslContext);
            } catch (Exception exception) {
                throw new IllegalStateException("Unable to restore the default SSL context.", exception);
            }
        }
    }

    private static void startDirectoryServer() {
        if (directoryServer != null) {
            return;
        }
        try {
            previousEndpointIdentification =
                    System.getProperty("com.sun.jndi.ldap.object.disableEndpointIdentification");
            previousSslContext = SSLContext.getDefault();

            @SuppressWarnings("unchecked")
            ObjectPair<File, char[]> certificate =
                    SelfSignedCertificateGenerator.generateTemporarySelfSignedCertificate("changeit", "PKCS12");
            keyStoreFile = certificate.getFirst();
            char[] keyStorePassword = certificate.getSecond();

            System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
            SSLContext.setDefault(new SSLUtil(new TrustAllTrustManager()).createSSLContext());

            SSLUtil sslUtil = new SSLUtil(
                    new KeyStoreKeyManager(keyStoreFile, keyStorePassword, "PKCS12", null),
                    new TrustAllTrustManager());
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(BASE_DN);
            config.setSchema(null);
            config.addAdditionalBindCredentials(SERVICE_DN, SERVICE_PASSWORD);
            config.setListenerConfigs(InMemoryListenerConfig.createLDAPSConfig(
                    "ldaps", 0, sslUtil.createSSLContext().getServerSocketFactory()));

            directoryServer = new InMemoryDirectoryServer(config);
            directoryServer.add(new Entry("dc=reboot,dc=com",
                    new Attribute("objectClass", "top", "domain"),
                    new Attribute("dc", "reboot")));
            directoryServer.add(new Entry(SERVICE_DN,
                    new Attribute("objectClass", "top", "person", "organizationalPerson", "inetOrgPerson"),
                    new Attribute("cn", "service"),
                    new Attribute("sn", "service"),
                    new Attribute("userPassword", SERVICE_PASSWORD)));
            directoryServer.add(new Entry("ou=Users," + BASE_DN,
                    new Attribute("objectClass", "top", "organizationalUnit"),
                    new Attribute("ou", "Users")));
            directoryServer.add(new Entry("ou=Groups," + BASE_DN,
                    new Attribute("objectClass", "top", "organizationalUnit"),
                    new Attribute("ou", "Groups")));
            directoryServer.add(new Entry(USER_DN,
                    new Attribute("objectClass", "top", "person", "organizationalPerson", "inetOrgPerson", "user"),
                    new Attribute("cn", "Julian Soto"),
                    new Attribute("sn", "Soto"),
                    new Attribute("displayName", "Julian Soto"),
                    new Attribute("sAMAccountName", "jsoto"),
                    new Attribute("userPrincipalName", "jsoto@example.test"),
                    new Attribute("mail", "jsoto@example.test"),
                    new Attribute("userPassword", USER_PASSWORD),
                    new Attribute("memberOf", GROUP_DN)));
            directoryServer.startListening();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to start embedded LDAPS test server.", exception);
        }
    }

    private static void restoreSystemProperty(String property, String value) {
        if (value == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, value);
        }
    }
}
