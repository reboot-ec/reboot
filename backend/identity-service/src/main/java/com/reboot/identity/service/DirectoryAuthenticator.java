package com.reboot.identity.service;

import com.reboot.identity.model.User;
import java.util.Set;

public interface DirectoryAuthenticator {

    /**
     * Authenticates a user against Active Directory using username/upn and password,
     * synchronizes user profile and roles in IdentityDB, and returns the persisted User entity.
     */
    User authenticateAndSynchronize(String usernameOrUpn, String password);

    /**
     * Verifies connectivity to the LDAPS server using service bind credentials without exposing secrets.
     */
    boolean testConnectivity();
}
