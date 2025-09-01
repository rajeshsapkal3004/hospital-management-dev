package com.hospital_management.services.userservice;

import com.hospital_management.dtos.*;
import com.hospital_management.models.Admin;
import com.hospital_management.models.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminService {

    // Register a new Admin
    AdminDto registerAdmin(AdminRegistrationDto registrationDto);

    // Get Admin by ID
    AdminDto getAdminById(Long id);

    // Get Admin by employeeId
    AdminDto getAdminByEmployeeId(String employeeId);

    // Get Admin by extension number
    AdminDto getAdminByExtensionNumber(String extensionNumber);

    // Update Admin details
    AdminDto updateAdmin(Long id, AdminRegistrationDto updateDto);

    // Permission updates
    void updateUserManagementPermission(Long adminId, boolean canManageUsers);

    void updateFinancialDataAccessPermission(Long adminId, boolean canAccess);

    void updateReportGenerationPermission(Long adminId, boolean canGenerate);

    void updateSystemSettingsPermission(Long adminId, boolean canModify);

    void updateSuperAdminStatus(Long adminId, boolean isSuperAdmin);

    void updateAdminLevel(Long adminId, Admin.AdminLevel adminLevel);

    void updateOfficeLocation(Long adminId, String officeLocation);

    void updateExtensionNumber(Long adminId, String extensionNumber);

    // Search & filter
    Page<AdminDto> getAllAdmins(Pageable pageable);

    Page<AdminDto> searchAdmins(String department, Admin.AdminLevel adminLevel, String jobTitle,
                                Integer minExperience, Boolean canManageUsers, Boolean canAccessFinancialData,
                                Boolean isSuperAdmin, Pageable pageable);

    List<AdminDto> getAdminsByDepartment(String department);

    List<AdminDto> getAdminsByLevel(Admin.AdminLevel adminLevel);

    List<AdminDto> getAdminsByJobTitle(String jobTitle);

    List<AdminDto> getAdminsBySupervisor(String supervisor);

    List<AdminDto> getAdminsWithManagedDepartments();

    // Validation
    boolean existsByEmployeeId(String employeeId);

    boolean existsByExtensionNumber(String extensionNumber);

    boolean isEmployeeIdAvailable(String employeeId, Long excludeAdminId);

    boolean isExtensionNumberAvailable(String extensionNumber, Long excludeAdminId);

    // Statistics
    long getTotalAdminCount();

    long getActiveAdminCount();

    long getSuperAdminCount();

    Map<String, Long> getAdminCountByDepartment();

    Map<String, Long> getAdminCountByLevel();

    Map<String, Long> getAdminCountByJobTitle();

    Map<String, Long> getAdminCountByOfficeLocation();

    Map<String, Long> getAdminCountBySupervisor();

    Map<String, Long> getAdminCountByManagedDepartment();

    Map<String, Long> getPermissionStatistics();

    // Dashboard
    AdminDashboardDto getDashboardData();
    SystemStatsDto getSystemStatistics();

    // User Management
    Page<UserDto> getAllUsersWithFilters(String search, Role.RoleName role,
                                         Boolean enabled, Boolean locked, Pageable pageable);
    BulkOperationResultDto bulkCreateUsers(MultipartFile file);
    BulkOperationResultDto performBulkUserAction(BulkUserActionDto actionDto);

    // Role Management
    List<RoleDto> getAllRoles();
    RoleDto createRole(CreateRoleDto roleDto);
    RoleDto updateRolePermissions(Long roleId, UpdateRolePermissionsDto permissionsDto);

    // Permission Management
    List<PermissionDto> getAllPermissions(String module, String category);

    // Department Management
    List<DepartmentDto> getAllDepartments();
    DepartmentDto createDepartment(CreateDepartmentDto departmentDto);
    DepartmentDto updateDepartment(Long id, UpdateDepartmentDto updateDto);
    void deleteDepartment(Long id);

    // Reports
    UserActivityReportDto getUserActivityReport(LocalDateTime startDate, LocalDateTime endDate);
    SystemUsageReportDto getSystemUsageReport(int days);
    String exportAuditLogs(AuditLogExportDto exportDto);

    // Emergency Operations
    String emergencyPasswordReset(Long userId);
    int emergencyUnlockAllAccounts();

    // Notifications
    int sendBroadcastNotification(BroadcastNotificationDto notificationDto);
}
