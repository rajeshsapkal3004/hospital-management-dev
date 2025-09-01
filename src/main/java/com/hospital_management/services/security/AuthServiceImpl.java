package com.hospital_management.services.security;

import com.hospital_management.models.User;
import com.hospital_management.repo.UserRepository;
import com.hospital_management.services.auditlogs.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Override
    @Transactional
    public void recordSuccessfulLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLoginDate(LocalDateTime.now());
            user.setFailedLoginAttempts(0); // Reset failed attempts on successful login
            userRepository.save(user);

            auditService.logUserAction("LOGIN_SUCCESS", user.getId(), "Successful login");
            log.debug("Recorded successful login for user: {}", username);
        });
    }

    @Override
    @Transactional
    public void recordFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setAccountLockedDate(LocalDateTime.now());

                auditService.logUserAction("ACCOUNT_AUTO_LOCKED", user.getId(),
                        "Account locked due to " + failedAttempts + " failed login attempts");
                log.warn("Account locked for user: {} after {} failed attempts", username, failedAttempts);
            } else {
                auditService.logUserAction("LOGIN_FAILED", user.getId(),
                        "Failed login attempt #" + failedAttempts);
                log.warn("Failed login attempt #{} for user: {}", failedAttempts, username);
            }

            userRepository.save(user);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountLocked(String username) {
        return userRepository.findByUsername(username)
                .map(User::isAccountLocked)
                .orElse(false);
    }

    @Override
    @Transactional
    public void unlockAccount(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAccountLocked(false);
            user.setAccountLockedDate(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            auditService.logUserAction("ACCOUNT_UNLOCKED", user.getId(),
                    "Account unlocked by administrator");
            log.info("Account unlocked for user: {}", username);
        });
    }

    @Override
    @Transactional
    public void resetFailedLoginAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            log.debug("Reset failed login attempts for user: {}", username);
        });
    }
}
