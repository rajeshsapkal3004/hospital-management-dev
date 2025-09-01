package com.hospital_management.dtos;


import com.hospital_management.models.Admin;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AdminRegistrationDto extends UserRegistrationDto {

    @NotBlank(message = "Employee ID is required for admin")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    private String employeeId;

    @Size(max = 20, message = "Extension number cannot exceed 20 characters")
    private String extensionNumber;

    @NotBlank(message = "Department is required for admin")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    @NotNull(message = "Admin Level is required")
    private Admin.AdminLevel adminLevel;

    @Size(max = 100, message = "Job title cannot exceed 100 characters")
    private String jobTitle;

    @Size(max = 100, message = "ReportsTo field cannot exceed 100 characters")
    private String reportsTo;

    @Size(max = 100, message = "Office location cannot exceed 100 characters")
    private String officeLocation;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Years of experience cannot exceed 60")
    private Integer yearsOfExperience;

    private Boolean canManageUsers = false;
    private Boolean canAccessFinancialData = false;
    private Boolean canGenerateReports = false;
    private Boolean canModifySystemSettings = false;
    private Boolean isSuperAdmin = false;

    private List<String> managedDepartments;

    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
}
