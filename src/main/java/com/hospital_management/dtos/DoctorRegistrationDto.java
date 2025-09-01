package com.hospital_management.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class DoctorRegistrationDto extends UserRegistrationDto {

    @NotBlank(message = "License number is required for doctors")
    @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
    private String licenseNumber;

    @NotBlank(message = "Specialization is required for doctors")
    @Size(min = 2, max = 100, message = "Specialization must be between 2 and 100 characters")
    private String specialization;

    @NotBlank(message = "Department is required for doctors")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    @Future(message = "License expiry date must be in the future")
    private LocalDate licenseExpiry;

    @Pattern(regexp = "^[0-9]{10}$", message = "NPI number must be exactly 10 digits")
    private String npiNumber;

    @Size(max = 20, message = "Employee ID cannot exceed 20 characters")
    private String employeeId;

    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be positive")
    @DecimalMax(value = "9999.99", message = "Consultation fee cannot exceed 9999.99")
    private BigDecimal consultationFee;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Years of experience cannot exceed 60")
    private Integer yearsOfExperience;

    @Size(max = 500, message = "Qualifications cannot exceed 500 characters")
    private String qualifications;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    private String roomNumber;
    private Set<String> certifications;
}
