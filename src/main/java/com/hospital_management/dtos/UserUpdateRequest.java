package com.hospital_management.dtos;


import com.hospital_management.models.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phone;

    private User.Gender gender;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
