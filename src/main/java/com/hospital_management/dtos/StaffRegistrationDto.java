package com.hospital_management.dtos;


import com.hospital_management.models.Role;
import com.hospital_management.models.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StaffRegistrationDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phone;

    @NotNull(message = "Role is required")
    private Role.RoleName role;

    private User.Gender gender;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    // Additional fields for staff
    private String employeeId;
    private String department;

    @AssertTrue(message = "Invalid role for staff registration")
    public boolean isValidStaffRole() {
        if (role == null) return false;

        // Only these roles are allowed for staff registration
        return role == Role.RoleName.DOCTOR ||
                role == Role.RoleName.NURSE ||
                role == Role.RoleName.LAB_TECHNICIAN ||
                role == Role.RoleName.RECEPTIONIST;
    }
}
