package com.hospital_management.usercontroller;

import com.hospital_management.config.UserPrincipal;
import com.hospital_management.dtos.*;
import com.hospital_management.models.Role;
import com.hospital_management.services.auditlogs.AuditService;
import com.hospital_management.services.system.SystemService;
import com.hospital_management.services.userservice.AdminService;
import com.hospital_management.services.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Admin Management", description = "Administrative operations and system management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final AuditService auditService;
    private final SystemService systemService;

    // ===============================
    // DASHBOARD & OVERVIEW
    // ===============================

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard data", description = "Retrieve comprehensive dashboard statistics")
    public ResponseEntity<AdminDashboardDto> getDashboard() {
        log.info("Fetching admin dashboard data");
        AdminDashboardDto dashboard = adminService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/stats/summary")
    @Operation(summary = "Get system statistics summary")
    public ResponseEntity<SystemStatsDto> getSystemStats() {
        log.info("Fetching system statistics");
        SystemStatsDto stats = adminService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }

    // ===============================
    // USER MANAGEMENT (BULK OPERATIONS)
    // ===============================

    @GetMapping("/users")
    @Operation(summary = "Get all users with advanced filtering")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role.RoleName role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Admin fetching users - search: {}, role: {}", search, role);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDto> users = adminService.getAllUsersWithFilters(
                search, role, enabled, locked, pageable);

        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/bulk-create")
    @Operation(summary = "Bulk create users from CSV/Excel")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> bulkCreateUsers(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Bulk user creation initiated by: {}", currentUser.getUsername());

            BulkOperationResultDto result = adminService.bulkCreateUsers(file);

            auditService.logUserAction("BULK_USER_CREATE", currentUser.getId(),
                    String.format("Bulk created %d users, %d failed",
                            result.getSuccessCount(), result.getFailureCount()));

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            log.error("Bulk user creation failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Bulk creation failed: " + e.getMessage()));
        }
    }

    @PutMapping("/users/bulk-action")
    @Operation(summary = "Perform bulk actions on users")
    public ResponseEntity<?> bulkUserAction(
            @Valid @RequestBody BulkUserActionDto actionDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Bulk user action: {} on {} users by: {}",
                    actionDto.getAction(), actionDto.getUserIds().size(), currentUser.getUsername());

            BulkOperationResultDto result = adminService.performBulkUserAction(actionDto);

            auditService.logUserAction("BULK_USER_ACTION", currentUser.getId(),
                    String.format("Bulk %s on %d users", actionDto.getAction(), actionDto.getUserIds().size()));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Bulk user action failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Bulk action failed: " + e.getMessage()));
        }
    }

    @GetMapping("/users/inactive")
    @Operation(summary = "Get inactive users")
    public ResponseEntity<List<UserDto>> getInactiveUsers(
            @RequestParam(defaultValue = "30") @Min(1) int days) {
        log.info("Fetching users inactive for {} days", days);
        List<UserDto> inactiveUsers = userService.getInactiveUsers(days);
        return ResponseEntity.ok(inactiveUsers);
    }

    // ===============================
    // ROLE & PERMISSION MANAGEMENT
    // ===============================

    @GetMapping("/roles")
    @Operation(summary = "Get all roles with permissions")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        log.info("Fetching all roles");
        List<RoleDto> roles = adminService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/roles")
    @Operation(summary = "Create new role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createRole(
            @Valid @RequestBody CreateRoleDto roleDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Creating new role: {} by: {}", roleDto.getName(), currentUser.getUsername());

            RoleDto createdRole = adminService.createRole(roleDto);

            auditService.logUserAction("ROLE_CREATE", currentUser.getId(),
                    "Created role: " + createdRole.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);

        } catch (Exception e) {
            log.error("Role creation failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Role creation failed: " + e.getMessage()));
        }
    }

    @PutMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Update role permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRolePermissionsDto permissionsDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Updating permissions for role: {} by: {}", roleId, currentUser.getUsername());

            RoleDto updatedRole = adminService.updateRolePermissions(roleId, permissionsDto);

            auditService.logUserAction("ROLE_PERMISSIONS_UPDATE", currentUser.getId(),
                    String.format("Updated permissions for role: %s", updatedRole.getName()));

            return ResponseEntity.ok(updatedRole);

        } catch (Exception e) {
            log.error("Role permissions update failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Permission update failed: " + e.getMessage()));
        }
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get all available permissions")
    public ResponseEntity<List<PermissionDto>> getAllPermissions(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String category) {
        log.info("Fetching permissions - module: {}, category: {}", module, category);
        List<PermissionDto> permissions = adminService.getAllPermissions(module, category);
        return ResponseEntity.ok(permissions);
    }

    // ===============================
    // DEPARTMENT MANAGEMENT
    // ===============================

    @GetMapping("/departments")
    @Operation(summary = "Get all departments")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        log.info("Fetching all departments");
        List<DepartmentDto> departments = adminService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @PostMapping("/departments")
    @Operation(summary = "Create new department")
    public ResponseEntity<?> createDepartment(
            @Valid @RequestBody CreateDepartmentDto departmentDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Creating department: {} by: {}", departmentDto.getName(), currentUser.getUsername());

            DepartmentDto createdDepartment = adminService.createDepartment(departmentDto);

            auditService.logUserAction("DEPARTMENT_CREATE", currentUser.getId(),
                    "Created department: " + createdDepartment.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);

        } catch (Exception e) {
            log.error("Department creation failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Department creation failed: " + e.getMessage()));
        }
    }

    @PutMapping("/departments/{id}")
    @Operation(summary = "Update department")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentDto updateDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Updating department: {} by: {}", id, currentUser.getUsername());

            DepartmentDto updatedDepartment = adminService.updateDepartment(id, updateDto);

            auditService.logUserAction("DEPARTMENT_UPDATE", currentUser.getId(),
                    "Updated department: " + updatedDepartment.getName());

            return ResponseEntity.ok(updatedDepartment);

        } catch (Exception e) {
            log.error("Department update failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Department update failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/departments/{id}")
    @Operation(summary = "Delete department")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.warn("Deleting department: {} by: {}", id, currentUser.getUsername());

            adminService.deleteDepartment(id);

            auditService.logUserAction("DEPARTMENT_DELETE", currentUser.getId(),
                    "Deleted department ID: " + id);

            return ResponseEntity.ok(new MessageResponse("Department deleted successfully"));

        } catch (Exception e) {
            log.error("Department deletion failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Department deletion failed: " + e.getMessage()));
        }
    }

    // ===============================
    // SYSTEM MONITORING
    // ===============================

    @GetMapping("/system/health")
    @Operation(summary = "Get system health status")
    public ResponseEntity<SystemHealthDto> getSystemHealth() {
        log.info("Checking system health");
        SystemHealthDto health = systemService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/system/performance")
    @Operation(summary = "Get system performance metrics")
    public ResponseEntity<SystemPerformanceDto> getSystemPerformance() {
        log.info("Fetching system performance metrics");
        SystemPerformanceDto performance = systemService.getPerformanceMetrics();
        return ResponseEntity.ok(performance);
    }

    @PostMapping("/system/maintenance-mode")
    @Operation(summary = "Toggle maintenance mode")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> toggleMaintenanceMode(
            @RequestParam boolean enable,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.warn("Maintenance mode {} by: {}", enable ? "enabled" : "disabled", currentUser.getUsername());

            systemService.setMaintenanceMode(enable, reason);

            auditService.logUserAction("MAINTENANCE_MODE_TOGGLE", currentUser.getId(),
                    String.format("Maintenance mode %s. Reason: %s", enable ? "enabled" : "disabled", reason));

            return ResponseEntity.ok(new MessageResponse(
                    "Maintenance mode " + (enable ? "enabled" : "disabled")));

        } catch (Exception e) {
            log.error("Maintenance mode toggle failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Maintenance mode toggle failed: " + e.getMessage()));
        }
    }

    // ===============================
    // AUDIT LOG MANAGEMENT
    // ===============================

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs with filtering")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Fetching audit logs - userId: {}, action: {}", userId, action);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLogDto> auditLogs = auditService.searchAuditLogs(
                userId, username, action, null, null, null, startDate, endDate, pageable);

        return ResponseEntity.ok(auditLogs);
    }

    @PostMapping("/audit-logs/export")
    @Operation(summary = "Export audit logs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> exportAuditLogs(
            @Valid @RequestBody AuditLogExportDto exportDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Exporting audit logs by: {}", currentUser.getUsername());

            String exportUrl = adminService.exportAuditLogs(exportDto);

            auditService.logUserAction("AUDIT_LOGS_EXPORT", currentUser.getId(),
                    "Exported audit logs for period: " + exportDto.getStartDate() + " to " + exportDto.getEndDate());

            return ResponseEntity.ok(Map.of("exportUrl", exportUrl));

        } catch (Exception e) {
            log.error("Audit logs export failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Export failed: " + e.getMessage()));
        }
    }

    // ===============================
    // REPORTS & ANALYTICS
    // ===============================

    @GetMapping("/reports/user-activity")
    @Operation(summary = "Get user activity report")
    public ResponseEntity<UserActivityReportDto> getUserActivityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {

        log.info("Generating user activity report for period: {} to {}", startDate, endDate);
        UserActivityReportDto report = adminService.getUserActivityReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/system-usage")
    @Operation(summary = "Get system usage analytics")
    public ResponseEntity<SystemUsageReportDto> getSystemUsageReport(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {

        log.info("Generating system usage report for last {} days", days);
        SystemUsageReportDto report = adminService.getSystemUsageReport(days);
        return ResponseEntity.ok(report);
    }

    // ===============================
    // DATA MANAGEMENT
    // ===============================

    @PostMapping("/data/backup")
    @Operation(summary = "Create system backup")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createBackup(
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.warn("System backup initiated by: {}", currentUser.getUsername());

            BackupResultDto result = systemService.createBackup(description);

            auditService.logUserAction("SYSTEM_BACKUP", currentUser.getId(),
                    "System backup created: " + result.getBackupId());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("System backup failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Backup failed: " + e.getMessage()));
        }
    }

    @GetMapping("/data/backups")
    @Operation(summary = "List available backups")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BackupInfoDto>> getBackups() {
        log.info("Fetching backup list");
        List<BackupInfoDto> backups = systemService.getAvailableBackups();
        return ResponseEntity.ok(backups);
    }

    @PostMapping("/data/restore/{backupId}")
    @Operation(summary = "Restore from backup")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> restoreFromBackup(
            @PathVariable String backupId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.warn("System restore from backup {} initiated by: {}", backupId, currentUser.getUsername());

            systemService.restoreFromBackup(backupId);

            auditService.logUserAction("SYSTEM_RESTORE", currentUser.getId(),
                    "System restored from backup: " + backupId);

            return ResponseEntity.ok(new MessageResponse("System restore initiated"));

        } catch (Exception e) {
            log.error("System restore failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Restore failed: " + e.getMessage()));
        }
    }

    // ===============================
    // CONFIGURATION MANAGEMENT
    // ===============================

    @GetMapping("/config")
    @Operation(summary = "Get system configuration")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SystemConfigDto> getSystemConfig() {
        log.info("Fetching system configuration");
        SystemConfigDto config = systemService.getSystemConfiguration();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/config")
    @Operation(summary = "Update system configuration")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateSystemConfig(
            @Valid @RequestBody SystemConfigDto configDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.warn("System configuration update by: {}", currentUser.getUsername());

            SystemConfigDto updatedConfig = systemService.updateSystemConfiguration(configDto);

            auditService.logUserAction("SYSTEM_CONFIG_UPDATE", currentUser.getId(),
                    "System configuration updated");

            return ResponseEntity.ok(updatedConfig);

        } catch (Exception e) {
            log.error("System configuration update failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Configuration update failed: " + e.getMessage()));
        }
    }

    // ===============================
    // EMERGENCY OPERATIONS
    // ===============================

    @PostMapping("/emergency/reset-user-password/{userId}")
    @Operation(summary = "Emergency password reset")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> emergencyPasswordReset(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.warn("Emergency password reset for user {} by: {}", userId, currentUser.getUsername());

            String tempPassword = adminService.emergencyPasswordReset(userId);

            auditService.logUserAction("EMERGENCY_PASSWORD_RESET", currentUser.getId(),
                    "Emergency password reset for user: " + userId);

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully",
                    "temporaryPassword", tempPassword
            ));

        } catch (Exception e) {
            log.error("Emergency password reset failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Password reset failed: " + e.getMessage()));
        }
    }

    @PostMapping("/emergency/unlock-all-accounts")
    @Operation(summary = "Emergency unlock all locked accounts")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> emergencyUnlockAllAccounts(
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.warn("Emergency unlock all accounts by: {} - Reason: {}", currentUser.getUsername(), reason);

            int unlockedCount = adminService.emergencyUnlockAllAccounts();

            auditService.logUserAction("EMERGENCY_UNLOCK_ALL", currentUser.getId(),
                    String.format("Emergency unlock all accounts. Count: %d. Reason: %s", unlockedCount, reason));

            return ResponseEntity.ok(Map.of(
                    "message", "All accounts unlocked successfully",
                    "unlockedCount", unlockedCount
            ));

        } catch (Exception e) {
            log.error("Emergency unlock all failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Emergency unlock failed: " + e.getMessage()));
        }
    }

    // ===============================
    // NOTIFICATION MANAGEMENT
    // ===============================

    @PostMapping("/notifications/broadcast")
    @Operation(summary = "Send broadcast notification to all users")
    public ResponseEntity<?> sendBroadcastNotification(
            @Valid @RequestBody BroadcastNotificationDto notificationDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Broadcasting notification by: {}", currentUser.getUsername());

            int recipientCount = adminService.sendBroadcastNotification(notificationDto);

            auditService.logUserAction("BROADCAST_NOTIFICATION", currentUser.getId(),
                    String.format("Broadcast notification sent to %d users: %s", recipientCount, notificationDto.getTitle()));

            return ResponseEntity.ok(Map.of(
                    "message", "Notification sent successfully",
                    "recipientCount", recipientCount
            ));

        } catch (Exception e) {
            log.error("Broadcast notification failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Notification failed: " + e.getMessage()));
        }
    }
}
