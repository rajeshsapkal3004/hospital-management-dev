package com.hospital_management.usercontroller;

import com.hospital_management.config.UserPrincipal;
import com.hospital_management.dtos.*;
import com.hospital_management.models.Role;
import com.hospital_management.services.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User Management", description = "APIs for user registration and management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ===============================
    // PUBLIC REGISTRATION (No Authentication Required)
    // ===============================

    @PostMapping("/register/patient")
    @Operation(summary = "Register a new patient", description = "Public patient registration")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody PatientSelfRegistrationDto registrationDto) {
        try {
            log.info("Public patient registration attempt for: {}", registrationDto.getEmail());

            // Convert to UserRegistrationDto and set PATIENT role
            UserRegistrationDto userDto = convertToUserRegistrationDto(registrationDto);
            userDto.setRole(Role.RoleName.PATIENT);

            UserDto createdUser = userService.registerUser(userDto);

            log.info("Patient registered successfully with ID: {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Patient registration successful. Please verify your email."));

        } catch (Exception e) {
            log.error("Error during patient registration: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }

    // ===============================
    // STAFF REGISTRATION (Admin/Receptionist Required)
    // ===============================

    @PostMapping("/register/staff")
    @Operation(summary = "Register staff member", description = "Register staff member by Admin/Receptionist")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<?> registerStaff(
            @Valid @RequestBody StaffRegistrationDto registrationDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Staff registration attempt for role: {} by user: {}",
                    registrationDto.getRole(), currentUser.getUsername());

            // Validate role assignment permissions
            if (!canAssignRole(currentUser, registrationDto.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("You don't have permission to assign this role"));
            }

            UserRegistrationDto userDto = convertToUserRegistrationDto(registrationDto);
            userDto.setRole(registrationDto.getRole());

            UserDto createdUser = userService.registerUser(userDto);

            log.info("Staff registered successfully with ID: {} and role: {}",
                    createdUser.getId(), registrationDto.getRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        } catch (Exception e) {
            log.error("Error during staff registration: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }

    // ===============================
    // ADMIN REGISTRATION (Super Admin Only)
    // ===============================

    @PostMapping("/register/admin")
    @Operation(summary = "Register a new admin user", description = "Register a new admin user (Super Admin only)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Admin registration attempt by super admin: {}", currentUser.getUsername());

            // Force ADMIN role
            registrationDto.setRole(Role.RoleName.ADMIN);

            UserDto createdUser = userService.registerAdmin(registrationDto);

            log.info("Admin registered successfully with ID: {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        } catch (Exception e) {
            log.error("Error during admin registration: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Admin registration failed: " + e.getMessage()));
        }
    }

    // ===============================
    // SUPER ADMIN REGISTRATION (System Setup Only)
    // ===============================

    @PostMapping("/register/super-admin")
    @Operation(summary = "Register super admin", description = "System setup - register first super admin")
    public ResponseEntity<?> registerSuperAdmin(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            @RequestParam String systemSetupKey) {
        try {
            log.warn("Super admin registration attempt");

            // Check if super admin already exists
            if (userService.superAdminExists()) {
                log.warn("Super admin registration denied - super admin already exists");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Super admin already exists"));
            }

            // Validate system setup key
            if (!userService.validateSystemSetupKey(systemSetupKey)) {
                log.warn("Super admin registration denied - invalid setup key");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Invalid system setup key"));
            }

            // Set SUPER_ADMIN role (not ADMIN)
            registrationDto.setRole(Role.RoleName.ADMIN);
            UserDto createdUser = userService.registerSuperAdmin(registrationDto);

            log.warn("SUPER ADMIN registered successfully with ID: {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        } catch (Exception e) {
            log.error("Error during super admin registration: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Super admin registration failed: " + e.getMessage()));
        }
    }

    // ===============================
    // GENERAL USER REGISTRATION (Admin/Receptionist)
    // ===============================

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new user in the hospital management system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            log.info("User registration attempt for username: {}", registrationDto.getUsername());

            UserDto createdUser = userService.registerUser(registrationDto);

            log.info("User registered successfully with ID: {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }

    // ===============================
    // USER PROFILE MANAGEMENT
    // ===============================

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Get the profile of the currently authenticated user")
    public ResponseEntity<UserDto> getCurrentUserProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.debug("Fetching profile for user: {}", currentUser.getUsername());

        UserDto userDto = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Update the profile of the currently authenticated user")
    public ResponseEntity<?> updateCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        try {
            log.info("Profile update attempt for user: {}", currentUser.getUsername());

            UserRegistrationDto updateDto = mapToRegistrationDto(updateRequest);
            UserDto updatedUser = userService.updateUser(currentUser.getId(), updateDto);

            log.info("Profile updated successfully for user: {}", currentUser.getUsername());
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            log.error("Error updating profile for user {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Profile update failed: " + e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for the currently authenticated user")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            log.info("Password change attempt for user: {}", currentUser.getUsername());

            userService.changePassword(
                    currentUser.getId(),
                    changePasswordRequest.getCurrentPassword(),
                    changePasswordRequest.getNewPassword()
            );

            log.info("Password changed successfully for user: {}", currentUser.getUsername());
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));

        } catch (Exception e) {
            log.error("Error changing password for user {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Password change failed: " + e.getMessage()));
        }
    }

    // ===============================
    // USER MANAGEMENT (ADMIN)
    // ===============================

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details by user ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.debug("Fetching user with ID: {}", id);

        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user by ID", description = "Update user details by user ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRegistrationDto updateDto) {
        try {
            log.info("Admin update attempt for user ID: {}", id);

            UserDto updatedUser = userService.updateUser(id, updateDto);

            log.info("User updated successfully: {}", id);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            log.error("Error updating user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Update failed: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Get paginated list of all users (Admin/Receptionist only)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.debug("Fetching all users - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users with various filters (Admin/Receptionist only)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.debug("Searching users with filters");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDto> users = userService.searchUsers(firstName, lastName, email, username, enabled, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role/{roleName}")
    @Operation(summary = "Get users by role", description = "Get all users with a specific role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByRole(
            @PathVariable com.hospital_management.models.Role.RoleName roleName) {

        log.debug("Fetching users by role: {}", roleName);

        List<UserDto> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    // ===============================
    // USER STATUS MANAGEMENT
    // ===============================

    @PutMapping("/{id}/enable")
    @Operation(summary = "Enable user account", description = "Enable a user account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        try {
            log.info("Enabling user account: {}", id);

            userService.enableUser(id);

            log.info("User account enabled successfully: {}", id);
            return ResponseEntity.ok(new MessageResponse("User account enabled successfully"));

        } catch (Exception e) {
            log.error("Error enabling user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to enable user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "Disable user account", description = "Disable a user account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        try {
            log.info("Disabling user account: {}", id);

            userService.disableUser(id);

            log.info("User account disabled successfully: {}", id);
            return ResponseEntity.ok(new MessageResponse("User account disabled successfully"));

        } catch (Exception e) {
            log.error("Error disabling user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to disable user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/lock")
    @Operation(summary = "Lock user account", description = "Lock a user account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> lockUser(@PathVariable Long id) {
        try {
            log.info("Locking user account: {}", id);

            userService.lockUserAccount(id);

            log.info("User account locked successfully: {}", id);
            return ResponseEntity.ok(new MessageResponse("User account locked successfully"));

        } catch (Exception e) {
            log.error("Error locking user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to lock user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/unlock")
    @Operation(summary = "Unlock user account", description = "Unlock a user account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        try {
            log.info("Unlocking user account: {}", id);

            userService.unlockUserAccount(id);

            log.info("User account unlocked successfully: {}", id);
            return ResponseEntity.ok(new MessageResponse("User account unlocked successfully"));

        } catch (Exception e) {
            log.error("Error unlocking user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to unlock user: " + e.getMessage()));
        }
    }

    // ===============================
    // ROLE MANAGEMENT
    // ===============================

    @PutMapping("/{id}/roles/{roleName}/assign")
    @Operation(summary = "Assign role to user", description = "Assign a role to a user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRoleToUser(
            @PathVariable Long id,
            @PathVariable com.hospital_management.models.Role.RoleName roleName) {
        try {
            log.info("Assigning role {} to user {}", roleName, id);

            userService.assignRoleToUser(id, roleName);

            log.info("Role {} assigned successfully to user {}", roleName, id);
            return ResponseEntity.ok(new MessageResponse("Role assigned successfully"));

        } catch (Exception e) {
            log.error("Error assigning role {} to user {}: {}", roleName, id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to assign role: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    @Operation(summary = "Remove role from user", description = "Remove a role from a user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeRoleFromUser(
            @PathVariable Long id,
            @PathVariable com.hospital_management.models.Role.RoleName roleName) {
        try {
            log.info("Removing role {} from user {}", roleName, id);

            userService.removeRoleFromUser(id, roleName);

            log.info("Role {} removed successfully from user {}", roleName, id);
            return ResponseEntity.ok(new MessageResponse("Role removed successfully"));

        } catch (Exception e) {
            log.error("Error removing role {} from user {}: {}", roleName, id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to remove role: " + e.getMessage()));
        }
    }

    // ===============================
    // STATISTICS AND REPORTS
    // ===============================

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Get various user statistics (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.debug("Fetching user statistics");

        Map<String, Object> statistics = Map.of(
                "totalUsers", userService.getTotalUserCount(),
                "activeUsers", userService.getActiveUserCount(),
                "usersByRole", userService.getUserCountByRole()
        );

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get inactive users", description = "Get users who haven't logged in for specified days (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getInactiveUsers(
            @RequestParam(defaultValue = "30") @Min(1) int days) {

        log.debug("Fetching users inactive for {} days", days);

        List<UserDto> inactiveUsers = userService.getInactiveUsers(days);
        return ResponseEntity.ok(inactiveUsers);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Permanently delete a user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            log.warn("User deletion attempt for ID: {}", id);

            userService.deleteUser(id);

            log.warn("User deleted successfully: {}", id);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));

        } catch (Exception e) {
            log.error("Error deleting user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to delete user: " + e.getMessage()));
        }
    }

    // ===============================
    // UTILITY METHODS
    // ===============================

    @GetMapping("/username/{username}/available")
    @Operation(summary = "Check username availability", description = "Check if a username is available for registration")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(
            @PathVariable String username) {

        boolean isAvailable = userService.isUsernameAvailable(username, null);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    @GetMapping("/email/{email}/available")
    @Operation(summary = "Check email availability", description = "Check if an email is available for registration")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(
            @PathVariable String email) {

        boolean isAvailable = userService.isEmailAvailable(email, null);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    // ===============================
    // HELPER METHODS
    // ===============================

    private boolean canAssignRole(UserPrincipal currentUser, Role.RoleName targetRole) {
        // Check if current user can assign the target role
        boolean hasAdminRole = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean hasReceptionistRole = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_RECEPTIONIST"));

        // Admin can assign any role except SUPER_ADMIN
        if (hasAdminRole) {
            return targetRole != Role.RoleName.ADMIN;
        }

        // Receptionist can only assign PATIENT role
        if (hasReceptionistRole) {
            return targetRole == Role.RoleName.PATIENT;
        }

        return false;
    }

    private UserRegistrationDto convertToUserRegistrationDto(PatientSelfRegistrationDto patientDto) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername(patientDto.getUsername());
        dto.setEmail(patientDto.getEmail());
        dto.setPassword(patientDto.getPassword());
        dto.setFirstName(patientDto.getFirstName());
        dto.setLastName(patientDto.getLastName());
        dto.setPhone(patientDto.getPhone());
        dto.setGender(patientDto.getGender());
        dto.setAddress(patientDto.getAddress());
        dto.setCity(patientDto.getCity());
        dto.setState(patientDto.getState());
        dto.setCountry(patientDto.getCountry());
        dto.setPostalCode(patientDto.getPostalCode());
        return dto;
    }

    private UserRegistrationDto convertToUserRegistrationDto(StaffRegistrationDto staffDto) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername(staffDto.getUsername());
        dto.setEmail(staffDto.getEmail());
        dto.setPassword(staffDto.getPassword());
        dto.setFirstName(staffDto.getFirstName());
        dto.setLastName(staffDto.getLastName());
        dto.setPhone(staffDto.getPhone());
        dto.setGender(staffDto.getGender());
        dto.setAddress(staffDto.getAddress());
        dto.setCity(staffDto.getCity());
        dto.setState(staffDto.getState());
        dto.setCountry(staffDto.getCountry());
        dto.setPostalCode(staffDto.getPostalCode());
        return dto;
    }

    private UserRegistrationDto mapToRegistrationDto(UserUpdateRequest updateRequest) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFirstName(updateRequest.getFirstName());
        dto.setLastName(updateRequest.getLastName());
        dto.setEmail(updateRequest.getEmail());
        dto.setPhone(updateRequest.getPhone());
        dto.setGender(updateRequest.getGender());
        dto.setAddress(updateRequest.getAddress());
        dto.setCity(updateRequest.getCity());
        dto.setState(updateRequest.getState());
        dto.setCountry(updateRequest.getCountry());
        dto.setPostalCode(updateRequest.getPostalCode());
        return dto;
    }
}
