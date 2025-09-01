package com.hospital_management.services.userservice;


import com.hospital_management.dtos.UserDto;
import com.hospital_management.dtos.UserRegistrationDto;
import com.hospital_management.models.Role;
import com.hospital_management.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    // Registration and basic operations
    UserDto registerUser(UserRegistrationDto registrationDto);
    UserDto getUserById(Long id);
    UserDto getUserByUsername(String username);
    UserDto getUserByEmail(String email);
    Optional<User> findUserEntityByUsername(String username);

    // Update operations
    UserDto updateUser(Long id, UserRegistrationDto updateDto);
    UserDto updateProfile(Long userId, Map<String, String> profileData);
    void changePassword(Long userId, String oldPassword, String newPassword);

    // Status management
    void lockUserAccount(Long userId);
    void unlockUserAccount(Long userId);
    void enableUser(Long userId);
    void disableUser(Long userId);
    void verifyEmail(Long userId);
    void verifyPhone(Long userId);

    // Role management
    void assignRoleToUser(Long userId, Role.RoleName roleName);
    void removeRoleFromUser(Long userId, Role.RoleName roleName);

    // Authentication tracking
    void recordSuccessfulLogin(String username);
    void recordFailedLogin(String username);

    // Search and filtering
    Page<UserDto> getAllUsers(Pageable pageable);
    Page<UserDto> searchUsers(String firstName, String lastName, String email,
                              String username, Boolean enabled, Pageable pageable);
    List<UserDto> getUsersByRole(Role.RoleName roleName);
    List<UserDto> getInactiveUsers(int days);

    // Validation
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean isUsernameAvailable(String username, Long excludeUserId);
    boolean isEmailAvailable(String email, Long excludeUserId);

    // Deletion
    void deleteUser(Long id);
    void softDeleteUser(Long id);

    // Statistics
    long getTotalUserCount();
    long getActiveUserCount();
    Map<String, Long> getUserCountByRole();

    // New registration methods
    UserDto registerSuperAdmin(UserRegistrationDto registrationDto);
    UserDto registerAdmin(UserRegistrationDto registrationDto);
    boolean superAdminExists();
    boolean validateSystemSetupKey(String key);

    // Role assignment validation
    boolean canUserAssignRole(Long userId, Role.RoleName targetRole);
}
