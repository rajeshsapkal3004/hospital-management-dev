package com.hospital_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "admins", uniqueConstraints = {
        @UniqueConstraint(columnNames = "employee_id")
})
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("ADMIN")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class Admin extends User{

    @NotBlank(message = "Employee ID is required for administrators")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Employee ID can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;

    @NotBlank(message = "Department is required for administrators")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @NotBlank(message = "Admin level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level", nullable = false, length = 20)
    private AdminLevel adminLevel;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Size(max = 100, message = "Job title cannot exceed 100 characters")
    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Size(max = 100, message = "Reports to cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Supervisor name can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "reports_to", length = 100)
    private String reportsTo;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Builder.Default
    @Column(name = "can_manage_users", nullable = false)
    private Boolean canManageUsers = true;

    @Builder.Default
    @Column(name = "can_access_financial_data", nullable = false)
    private Boolean canAccessFinancialData = false;

    @Builder.Default
    @Column(name = "can_generate_reports", nullable = false)
    private Boolean canGenerateReports = true;

    @Builder.Default
    @Column(name = "can_modify_system_settings", nullable = false)
    private Boolean canModifySystemSettings = false;

    @Builder.Default
    @Column(name = "is_super_admin", nullable = false)
    private Boolean isSuperAdmin = false;

    @Size(max = 20, message = "Extension number cannot exceed 20 characters")
    @Pattern(regexp = "^[0-9-]+$", message = "Extension number can only contain numbers and hyphens")
    @Column(name = "extension_number", length = 20)
    private String extensionNumber;

    @Size(max = 50, message = "Office location cannot exceed 50 characters")
    @Column(name = "office_location", length = 50)
    private String officeLocation;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "admin_managed_departments",
            joinColumns = @JoinColumn(name = "admin_id")
    )
    @Column(name = "department", length = 100)
    @Size(max = 100, message = "Each managed department cannot exceed 100 characters")
    @Builder.Default
    private Set<String> managedDepartments = new HashSet<>();

    // Custom constructors
    public Admin(String username, String email, String password, String firstName, String lastName,
                 String employeeId, String department, AdminLevel adminLevel) {
        super(username, email, password, firstName, lastName);
        this.employeeId = employeeId;
        this.department = department;
        this.adminLevel = adminLevel;
        this.canManageUsers = true;
        this.canAccessFinancialData = false;
        this.canGenerateReports = true;
        this.canModifySystemSettings = false;
        this.isSuperAdmin = false;
        this.managedDepartments = new HashSet<>();
    }

    // Business methods
    public String getAdminCode() {
        return "ADM-" + this.employeeId;
    }

    public boolean canPerformAction(String action) {
        switch (action.toLowerCase()) {
            case "manage_users":
                return this.canManageUsers;
            case "access_financial_data":
                return this.canAccessFinancialData;
            case "generate_reports":
                return this.canGenerateReports;
            case "modify_system_settings":
                return this.canModifySystemSettings;
            default:
                return this.isSuperAdmin;
        }
    }

    public void addManagedDepartment(String department) {
        if (department != null && !department.trim().isEmpty()) {
            this.managedDepartments.add(department.trim());
        }
    }

    public void removeManagedDepartment(String department) {
        this.managedDepartments.remove(department);
    }

    public boolean canManageDepartment(String department) {
        return this.isSuperAdmin ||
                this.managedDepartments.isEmpty() ||
                this.managedDepartments.contains(department);
    }

    public enum AdminLevel {
        SYSTEM_ADMIN("System Administrator"),
        DEPARTMENT_ADMIN("Department Administrator"),
        DATA_ADMIN("Data Administrator"),
        SECURITY_ADMIN("Security Administrator"),
        HOSPITAL_ADMIN("Hospital Administrator");

        private final String displayName;

        AdminLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


    /**
     * Set super admin status
     * @param superAdmin true if super admin, false otherwise
     */
    public void setSuperAdmin(boolean superAdmin) {
        this.isSuperAdmin = superAdmin;
    }

    /**
     * Get super admin status
     * @return true if super admin, false otherwise
     */
    public boolean isSuperAdmin() {
        return this.isSuperAdmin;
    }

    /**
     * Set user management permission
     * @param canManageUsers true if can manage users, false otherwise
     */
    public void setCanManageUsers(boolean canManageUsers) {
        this.canManageUsers = canManageUsers;
    }

    /**
     * Get user management permission
     * @return true if can manage users, false otherwise
     */
    public boolean canManageUsers() {
        return this.canManageUsers;
    }

    /**
     * Set financial data access permission
     * @param canAccessFinancialData true if can access financial data, false otherwise
     */
    public void setCanAccessFinancialData(boolean canAccessFinancialData) {
        this.canAccessFinancialData = canAccessFinancialData;
    }

    /**
     * Get financial data access permission
     * @return true if can access financial data, false otherwise
     */
    public boolean canAccessFinancialData() {
        return this.canAccessFinancialData;
    }

    /**
     * Set report generation permission
     * @param canGenerateReports true if can generate reports, false otherwise
     */
    public void setCanGenerateReports(boolean canGenerateReports) {
        this.canGenerateReports = canGenerateReports;
    }

    /**
     * Get report generation permission
     * @return true if can generate reports, false otherwise
     */
    public boolean canGenerateReports() {
        return this.canGenerateReports;
    }

    /**
     * Set system settings modification permission
     * @param canModifySystemSettings true if can modify system settings, false otherwise
     */
    public void setCanModifySystemSettings(boolean canModifySystemSettings) {
        this.canModifySystemSettings = canModifySystemSettings;
    }

    /**
     * Get system settings modification permission
     * @return true if can modify system settings, false otherwise
     */
    public boolean canModifySystemSettings() {
        return this.canModifySystemSettings;
    }

//    /**
//     * Add a managed department to the admin
//     * @param department The department to add
//     */
//    public void addManagedDepartment(String department) {
//        if (department != null && !department.trim().isEmpty()) {
//            if (this.managedDepartments == null) {
//                this.managedDepartments = new ArrayList<>();
//            }
//            String trimmedDept = department.trim();
//            if (!this.managedDepartments.contains(trimmedDept)) {
//                this.managedDepartments.add(trimmedDept);
//            }
//        }
//    }



    /**
     * Check if admin manages a specific department
     * @param department The department to check
     * @return true if manages the department, false otherwise
     */
    public boolean managesDepartment(String department) {
        return this.managedDepartments != null &&
                department != null &&
                this.managedDepartments.contains(department.trim());
    }

    /**
     * Get count of managed departments
     * @return Number of managed departments
     */
    public int getManagedDepartmentCount() {
        return this.managedDepartments != null ? this.managedDepartments.size() : 0;
    }

    /**
     * Check if admin has any management permissions
     * @return true if has any management permission, false otherwise
     */
    public boolean hasManagementPermissions() {
        return this.canManageUsers ||
                this.canAccessFinancialData ||
                this.canGenerateReports ||
                this.canModifySystemSettings ||
                this.isSuperAdmin;
    }

    /**
     * Check if admin has full administrative privileges
     * @return true if has all permissions or is super admin, false otherwise
     */
    public boolean hasFullAdminPrivileges() {
        return this.isSuperAdmin ||
                (this.canManageUsers &&
                        this.canAccessFinancialData &&
                        this.canGenerateReports &&
                        this.canModifySystemSettings);
    }

    /**
     * Set all permissions to the specified value
     * @param value true to grant all permissions, false to revoke all
     */
    public void setAllPermissions(boolean value) {
        this.canManageUsers = value;
        this.canAccessFinancialData = value;
        this.canGenerateReports = value;
        this.canModifySystemSettings = value;
    }

    /**
     * Check if admin level allows specific operations
     * @return true if high-level admin (SENIOR or EXECUTIVE), false otherwise
     */
    public boolean isHighLevelAdmin() {
        return this.adminLevel == AdminLevel.SYSTEM_ADMIN ||
                this.adminLevel == AdminLevel.HOSPITAL_ADMIN;
    }




}
