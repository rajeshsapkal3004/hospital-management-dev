package com.hospital_management.dtos;


import com.hospital_management.models.Admin;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminDto extends UserDto {

    private String employeeId;
    private String extensionNumber;
    private String department;
    private Admin.AdminLevel adminLevel;
    private String jobTitle;
    private String reportsTo;
    private String officeLocation;
    private Integer yearsOfExperience;
    private boolean canManageUsers;
    private boolean canAccessFinancialData;
    private boolean canGenerateReports;
    private boolean canModifySystemSettings;
    private boolean isSuperAdmin;
    private List<String> managedDepartments;
    private LocalDate hireDate;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
