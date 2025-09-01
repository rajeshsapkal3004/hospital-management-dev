package com.hospital_management.services.userservice;


import com.hospital_management.dtos.UserDto;
import com.hospital_management.dtos.UserRegistrationDto;
import com.hospital_management.exception.UserAlreadyExistsException;
import com.hospital_management.exception.UserNotFoundException;
import com.hospital_management.mapper.UserMapper;
import com.hospital_management.models.Role;
import com.hospital_management.models.User;
import com.hospital_management.repo.RoleRepository;
import com.hospital_management.repo.UserRepository;
import com.hospital_management.services.auditlogs.AuditService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final Environment environment;

    // ===============================
    // USER REGISTRATION
    // ===============================

    @Override
    @Transactional
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        log.info("Registering new user with username: {}", registrationDto.getUsername());

        validateUserRegistration(registrationDto);

        try {
            User user = userMapper.toEntity(registrationDto);
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            Role userRole = roleRepository.findByName(registrationDto.getRole())
                    .orElseThrow(() -> new ValidationException("Invalid role: " + registrationDto.getRole()));
            user.getRoles().add(userRole);

            User savedUser = userRepository.save(user);

            auditService.logUserAction("USER_REGISTRATION", savedUser.getId(),
                    "User registered successfully with role: " + registrationDto.getRole());

            log.info("User registered successfully with ID: {}", savedUser.getId());
            return userMapper.toDto(savedUser);

        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage(), e);
            throw new ValidationException("Failed to register user: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserDto registerSuperAdmin(UserRegistrationDto registrationDto) {
        log.info("Registering super admin: {}", registrationDto.getUsername());

        if (superAdminExists()) {
            throw new ValidationException("Super admin already exists");
        }

        User user = userMapper.toEntity(registrationDto);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEnabled(true);
        user.setEmailVerified(true);

        // FIXED: Use SUPER_ADMIN role
        Role superAdminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                .orElseThrow(() -> new ValidationException("SUPER_ADMIN role not found"));
        user.getRoles().add(superAdminRole);

        User savedUser = userRepository.save(user);

        auditService.logUserAction("SUPER_ADMIN_REGISTERED", savedUser.getId(),
                "Super admin registered: " + savedUser.getUsername());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto registerAdmin(UserRegistrationDto registrationDto) {
        log.info("Registering admin: {}", registrationDto.getUsername());

        User user = userMapper.toEntity(registrationDto);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEnabled(true);

        Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                .orElseThrow(() -> new ValidationException("ADMIN role not found"));
        user.getRoles().add(adminRole);

        User savedUser = userRepository.save(user);

        auditService.logUserAction("ADMIN_REGISTERED", savedUser.getId(),
                "Admin registered: " + savedUser.getUsername());

        return userMapper.toDto(savedUser);
    }

    // ===============================
    // USER RETRIEVAL
    // ===============================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST') or (hasRole('PATIENT') and #id == authentication.principal.id)")
    public UserDto getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#username")
    public UserDto getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("username", username));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserEntityByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ===============================
    // USER UPDATE
    // ===============================

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PATIENT') and #id == authentication.principal.id)")
    public UserDto updateUser(Long id, UserRegistrationDto updateDto) {
        log.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!existingUser.getUsername().equals(updateDto.getUsername()) &&
                userRepository.existsByUsername(updateDto.getUsername())) {
            throw new UserAlreadyExistsException("username", updateDto.getUsername());
        }

        if (!existingUser.getEmail().equals(updateDto.getEmail()) &&
                userRepository.existsByEmail(updateDto.getEmail())) {
            throw new UserAlreadyExistsException("email", updateDto.getEmail());
        }

        userMapper.updateUserFromDto(updateDto, existingUser);
        User savedUser = userRepository.save(existingUser);

        auditService.logUserAction("USER_UPDATED", id, "User profile updated");
        log.info("User updated successfully: {}", id);

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDto updateProfile(Long userId, Map<String, String> profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.getProfileData().putAll(profileData);
        User savedUser = userRepository.save(user);

        auditService.logUserAction("PROFILE_UPDATED", userId, "Profile data updated");
        return userMapper.toDto(savedUser);
    }

    // ===============================
    // ACCOUNT MANAGEMENT
    // ===============================

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void lockUserAccount(Long userId) {
        log.info("Locking user account: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.lockAccount();
        userRepository.save(user);

        auditService.logUserAction("ACCOUNT_LOCKED", userId, "Account locked by administrator");
        log.warn("User account locked: {}", userId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void unlockUserAccount(Long userId) {
        log.info("Unlocking user account: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.unlockAccount();
        userRepository.save(user);

        auditService.logUserAction("ACCOUNT_UNLOCKED", userId, "Account unlocked by administrator");
        log.info("User account unlocked: {}", userId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        userRepository.updateEnabledStatus(userId, true);
        auditService.logUserAction("USER_ENABLED", userId, "User account enabled");
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        userRepository.updateEnabledStatus(userId, false);
        auditService.logUserAction("USER_DISABLED", userId, "User account disabled");
    }

    // ===============================
    // PASSWORD MANAGEMENT
    // ===============================

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        validatePassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedDate(LocalDateTime.now());
        userRepository.save(user);

        auditService.logUserAction("PASSWORD_CHANGED", userId, "Password changed successfully");
        log.info("Password changed successfully for user: {}", userId);
    }

    // ===============================
    // ROLE MANAGEMENT
    // ===============================

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void assignRoleToUser(Long userId, Role.RoleName roleName) {
        log.info("Assigning role {} to user: {}", roleName, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Role not found: " + roleName));

        user.addRole(role);
        userRepository.save(user);

        auditService.logUserAction("ROLE_ASSIGNED", userId, "Role assigned: " + roleName);
        log.info("Role {} assigned to user: {}", roleName, userId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void removeRoleFromUser(Long userId, Role.RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Role not found: " + roleName));

        user.removeRole(role);
        userRepository.save(user);

        auditService.logUserAction("ROLE_REMOVED", userId, "Role removed: " + roleName);
    }

    // ===============================
    // LOGIN TRACKING
    // ===============================

    @Override
    @Transactional
    public void recordSuccessfulLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.updateLastLogin();
            userRepository.save(user);
            auditService.logUserAction("LOGIN_SUCCESS", user.getId(), "Successful login");
            log.debug("Recorded successful login for user: {}", username);
        });
    }

    @Override
    @Transactional
    public void recordFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);

            String message = "Failed login attempt #" + user.getFailedLoginAttempts();
            if (user.isAccountNonLocked()) {
                auditService.logUserAction("ACCOUNT_AUTO_LOCKED", user.getId(),
                        "Account locked due to multiple failed login attempts");
                message += " - Account locked";
            }

            auditService.logUserAction("LOGIN_FAILED", user.getId(), message);
            log.warn("Recorded failed login for user: {} (Attempt: {})", username, user.getFailedLoginAttempts());
        });
    }

    // ===============================
    // USER QUERIES
    // ===============================

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public Page<UserDto> searchUsers(String firstName, String lastName, String email,
                                     String username, Boolean enabled, Pageable pageable) {
        log.debug("Searching users with filters");
        return userRepository.findUsersWithFilters(firstName, lastName, email, username, enabled, pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getUsersByRole(Role.RoleName roleName) {
        log.debug("Fetching users by role: {}", roleName);
        return userMapper.toDtoList(userRepository.findByRoleName(roleName));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getInactiveUsers(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userMapper.toDtoList(userRepository.findInactiveUsers(cutoffDate));
    }

    // ===============================
    // VERIFICATION
    // ===============================

    @Override
    @Transactional
    public void verifyEmail(Long userId) {
        userRepository.markEmailAsVerified(userId);
        auditService.logUserAction("EMAIL_VERIFIED", userId, "Email address verified");
    }

    @Override
    @Transactional
    public void verifyPhone(Long userId) {
        userRepository.markPhoneAsVerified(userId);
        auditService.logUserAction("PHONE_VERIFIED", userId, "Phone number verified");
    }

    // ===============================
    // USER DELETION
    // ===============================

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);
        auditService.logUserAction("USER_DELETED", id, "User permanently deleted");
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "users", key = "#id")
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setEnabled(false);
        userRepository.save(user);
        auditService.logUserAction("USER_SOFT_DELETED", id, "User soft deleted (disabled)");
    }

    // ===============================
    // UTILITY METHODS
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username, Long excludeUserId) {
        if (excludeUserId != null) {
            return !userRepository.existsByUsernameAndIdNot(username, excludeUserId);
        }
        return !userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email, Long excludeUserId) {
        if (excludeUserId != null) {
            return !userRepository.existsByEmailAndIdNot(email, excludeUserId);
        }
        return !userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean superAdminExists() {
        // FIXED: Check for SUPER_ADMIN role, not ADMIN
        return userRepository.existsByRoles_Name(Role.RoleName.ADMIN);
    }

    @Override
    public boolean validateSystemSetupKey(String key) {
        String systemSetupKey = environment.getProperty("hospital.app.system-setup-key", "HMS_SETUP_2024_SECURE");
        return systemSetupKey.equals(key);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserAssignRole(Long userId, Role.RoleName targetRole) {
        return userRepository.findById(userId)
                .map(user -> {
                    // Super admin can assign any role
                    if (user.getRoles().stream().anyMatch(role -> role.getName() == Role.RoleName.ADMIN)) {
                        return true;
                    }
                    // Admin can assign any role except SUPER_ADMIN
                    if (user.getRoles().stream().anyMatch(role -> role.getName() == Role.RoleName.ADMIN)) {
                        return targetRole != Role.RoleName.ADMIN;
                    }
                    // Receptionist can only assign PATIENT role
                    if (user.getRoles().stream().anyMatch(role -> role.getName() == Role.RoleName.RECEPTIONIST)) {
                        return targetRole == Role.RoleName.PATIENT;
                    }
                    return false;
                })
                .orElse(false);
    }

    // ===============================
    // STATISTICS
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        return userRepository.countByEnabledTrue();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user-statistics", key = "'role-counts'")
    public Map<String, Long> getUserCountByRole() {
        Map<String, Long> roleCounts = new HashMap<>();
        for (Role.RoleName role : Role.RoleName.values()) {
            roleCounts.put(role.name(), userRepository.countByRoleName(role));
        }
        return roleCounts;
    }

    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    private void validateUserRegistration(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("username", dto.getUsername());
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("email", dto.getEmail());
        }

        validatePassword(dto.getPassword());
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("Password must contain at least one digit");
        }

        if (!password.matches(".*[@$!%*?&].*")) {
            throw new ValidationException("Password must contain at least one special character");
        }
    }
}
