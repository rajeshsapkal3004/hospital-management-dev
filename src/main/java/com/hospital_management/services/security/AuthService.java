package com.hospital_management.services.security;


public interface AuthService {
    void recordSuccessfulLogin(String username);
    void recordFailedLogin(String username);
    boolean isAccountLocked(String username);
    void unlockAccount(String username);
    void resetFailedLoginAttempts(String username);
}
