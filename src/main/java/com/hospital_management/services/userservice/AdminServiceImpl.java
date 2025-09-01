package com.hospital_management.services.userservice;



import com.hospital_management.dtos.*;
import com.hospital_management.enumclasses.SystemHealthStatus;
import com.hospital_management.exception.UserAlreadyExistsException;
import com.hospital_management.exception.UserNotFoundException;
import com.hospital_management.mapper.*;
import com.hospital_management.models.*;
import com.hospital_management.repo.*;
import com.hospital_management.services.auditlogs.AuditService;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminMapper adminMapper;
    private final AuditService auditService;
    private final UserService userService;

    private final AuditLogRepository auditLogRepository;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentRepository departmentRepository;
    private final PermissionMapper permissionMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public AdminDto registerAdmin(@Valid AdminRegistrationDto registrationDto) {
        log.info("Registering new admin with employee ID: {}", registrationDto.getEmployeeId());

        validateAdminRegistration(registrationDto);

        try {
            Admin admin = adminMapper.toEntity(registrationDto);
            admin.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            // Assign ADMIN role
            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                    .orElseThrow(() -> new ValidationException("ADMIN role not found"));
            admin.getRoles().add(adminRole);

            Admin savedAdmin = adminRepository.save(admin);

            auditService.logUserAction("ADMIN_REGISTRATION", savedAdmin.getId(),
                    "Admin registered with employee ID: " + registrationDto.getEmployeeId());

            log.info("Admin registered successfully with ID: {}", savedAdmin.getId());
            return adminMapper.toDto(savedAdmin);
        } catch (Exception e) {
            log.error("Error registering admin: {}", e.getMessage(), e);
            throw new ValidationException("Failed to register admin: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "admins", key = "#id")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public AdminDto getAdminById(Long id) {
        log.debug("Fetching admin by ID: {}", id);
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with ID: " + id));
        return adminMapper.toDto(admin);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "admins", key = "#employeeId")
    public AdminDto getAdminByEmployeeId(String employeeId) {
        log.debug("Fetching admin by employee ID: {}", employeeId);
        Admin admin = adminRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with employee ID: " + employeeId));
        return adminMapper.toDto(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDto getAdminByExtensionNumber(String extensionNumber) {
        log.debug("Fetching admin by extension number: {}", extensionNumber);
        Admin admin = adminRepository.findByExtensionNumber(extensionNumber)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with extension number: " + extensionNumber));
        return adminMapper.toDto(admin);
    }

    @Override
    @Transactional
    @CacheEvict(value = "admins", key = "#id")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public AdminDto updateAdmin(Long id, @Valid AdminRegistrationDto updateDto) {
        log.info("Updating admin with ID: {}", id);

        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with ID: " + id));

        validateAdminUpdate(updateDto, id);

        adminMapper.updateAdminFromDto(updateDto, existingAdmin);
        Admin savedAdmin = adminRepository.save(existingAdmin);

        auditService.logUserAction("ADMIN_UPDATED", id, "Admin profile updated");
        log.info("Admin updated successfully: {}", id);

        return adminMapper.toDto(savedAdmin);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public void updateUserManagementPermission(Long adminId, boolean canManageUsers) {
        updateBooleanPermission(adminId, canManageUsers, "User Management", (id, value) -> adminRepository.updateUserManagementPermission(id, value));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public void updateFinancialDataAccessPermission(Long adminId, boolean canAccess) {
        updateBooleanPermission(adminId, canAccess, "Financial Data Access", (id, value) -> adminRepository.updateFinancialDataAccessPermission(id, value));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public void updateReportGenerationPermission(Long adminId, boolean canGenerate) {
        updateBooleanPermission(adminId, canGenerate, "Report Generation", (id, value) -> adminRepository.updateReportGenerationPermission(id, value));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public void updateSystemSettingsPermission(Long adminId, boolean canModify) {
        updateBooleanPermission(adminId, canModify, "System Settings", (id, value) -> adminRepository.updateSystemSettingsPermission(id, value));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public void updateSuperAdminStatus(Long adminId, boolean isSuperAdmin) {
        log.info("Updating super admin status for admin {} to {}", adminId, isSuperAdmin);
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with ID: " + adminId));
        admin.setSuperAdmin(isSuperAdmin);
        adminRepository.save(admin);

        auditService.logUserAction("SUPER_ADMIN_STATUS_UPDATED", adminId,
                "Super admin status updated to: " + isSuperAdmin);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public void updateAdminLevel(Long adminId, Admin.AdminLevel adminLevel) {
        log.info("Updating admin level for admin {} to {}", adminId, adminLevel);
        adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with ID: " + adminId));
        adminRepository.updateAdminLevel(adminId, adminLevel);
        auditService.logUserAction("ADMIN_LEVEL_UPDATED", adminId,
                "Admin level updated to: " + adminLevel);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public void updateOfficeLocation(Long adminId, String officeLocation) {
        log.info("Updating office location for admin {} to {}", adminId, officeLocation);
        adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with ID: " + adminId));
        adminRepository.updateOfficeLocation(adminId, officeLocation);
        auditService.logUserAction("OFFICE_LOCATION_UPDATED", adminId,
                "Office location updated to: " + officeLocation);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public void updateExtensionNumber(Long adminId, String extensionNumber) {
        log.info("Updating extension number for admin {} to {}", adminId, extensionNumber);

        if (extensionNumber != null && adminRepository.existsByExtensionNumberAndIdNot(extensionNumber, adminId)) {
            throw new UserAlreadyExistsException("extension number", extensionNumber);
        }

        adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with ID: " + adminId));

        adminRepository.updateExtensionNumber(adminId, extensionNumber);
        auditService.logUserAction("EXTENSION_NUMBER_UPDATED", adminId,
                "Extension number updated to: " + extensionNumber);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Page<AdminDto> getAllAdmins(Pageable pageable) {
        log.debug("Fetching all admins");
        return adminRepository.findAll(pageable).map(adminMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Page<AdminDto> searchAdmins(String department, Admin.AdminLevel adminLevel, String jobTitle,
                                       Integer minExperience, Boolean canManageUsers, Boolean canAccessFinancialData,
                                       Boolean isSuperAdmin, Pageable pageable) {
        log.debug("Searching admins with filters");
        return adminRepository.findAdminsWithFilters(department, adminLevel, jobTitle, minExperience,
                canManageUsers, canAccessFinancialData, isSuperAdmin, pageable).map(adminMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDto> getAdminsByDepartment(String department) {
        return adminMapper.toDtoList(adminRepository.findByDepartment(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDto> getAdminsByLevel(Admin.AdminLevel adminLevel) {
        return adminMapper.toDtoList(adminRepository.findByAdminLevel(adminLevel));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDto> getAdminsByJobTitle(String jobTitle) {
        return adminMapper.toDtoList(adminRepository.findByJobTitle(jobTitle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDto> getAdminsBySupervisor(String supervisor) {
        return adminMapper.toDtoList(adminRepository.findByReportsTo(supervisor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDto> getAdminsWithManagedDepartments() {
        return adminMapper.toDtoList(adminRepository.findAdminsWithManagedDepartments());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return adminRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByExtensionNumber(String extensionNumber) {
        return adminRepository.existsByExtensionNumber(extensionNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmployeeIdAvailable(String employeeId, Long excludeAdminId) {
        if (excludeAdminId != null) {
            return !adminRepository.existsByEmployeeIdAndIdNot(employeeId, excludeAdminId);
        }
        return !adminRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExtensionNumberAvailable(String extensionNumber, Long excludeAdminId) {
        if (excludeAdminId != null) {
            return !adminRepository.existsByExtensionNumberAndIdNot(extensionNumber, excludeAdminId);
        }
        return !adminRepository.existsByExtensionNumber(extensionNumber);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "admin-statistics", key = "'total-count'")
    public long getTotalAdminCount() {
        return adminRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "admin-statistics", key = "'active-count'")
    public long getActiveAdminCount() {
        return adminRepository.countActiveAdmins();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "admin-statistics", key = "'super-admin-count'")
    public long getSuperAdminCount() {
        return adminRepository.countSuperAdmins();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAdminCountByDepartment() {
        List<Object[]> results = adminRepository.getAdminCountByDepartment();
        Map<String, Long> map = new HashMap<>();
        for (Object[] o : results) {
            map.put((String) o[0], (Long) o[1]);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAdminCountByLevel() {
        List<Object[]> results = adminRepository.getAdminCountByLevel();
        Map<String, Long> map = new HashMap<>();
        for (Object[] o : results) {
            map.put(o[0] != null ? o[0].toString() : "UNKNOWN", (Long) o[1]);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAdminCountByJobTitle() {
        List<Object[]> results = adminRepository.getAdminCountByJobTitle();
        Map<String, Long> map = new HashMap<>();
        for (Object[] o : results) {
            map.put((String) o[0], (Long) o[1]);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAdminCountByOfficeLocation() {
        List<Object[]> results = adminRepository.getAdminCountByOfficeLocation();
        Map<String, Long> map = new HashMap<>();
        for (Object[] o : results) {
            map.put((String) o[0], (Long) o[1]);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAdminCountBySupervisor() {
        List<Object[]> results = adminRepository.getAdminCountBySupervisor();
        Map<String, Long> map = new HashMap<>();
        for (Object[] o : results) {
            map.put((String) o[0], (Long) o[1]);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAdminCountByManagedDepartment() {
        List<Object[]> results = adminRepository.getAdminCountByManagedDepartment();
        Map<String, Long> map = new HashMap<>();
        for (Object[] o : results) {
            map.put((String) o[0], (Long) o[1]);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getPermissionStatistics() {
        List<Object[]> results = adminRepository.getPermissionStatistics();
        Map<String, Long> map = new HashMap<>();
        for (Object[] o : results) {
            String key = o[0].toString();
            Long val = (Long) o[1];
            map.put(key, val);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardData() {
        log.info("Fetching admin dashboard data");

        try {
            // Get current statistics
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByEnabledTrue();
            long totalPatients = userRepository.countByRoles_Name(Role.RoleName.PATIENT);
            long totalDoctors = userRepository.countByRoles_Name(Role.RoleName.DOCTOR);
            long todayAppointments = getTodayAppointmentsCount();
            long pendingApprovals = getPendingApprovalsCount();

            // Get recent activities
            List<RecentActivityDto> recentActivities = getRecentActivities();

            // Get additional statistics
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", totalUsers);
            statistics.put("activeUsers", activeUsers);
            statistics.put("inactiveUsers", totalUsers - activeUsers);
            statistics.put("lockedUsers", userRepository.countByAccountLockedTrue());
            statistics.put("totalRoles", roleRepository.count());
            statistics.put("totalPermissions", permissionRepository.count());
            statistics.put("totalDepartments", departmentRepository.count());

            return AdminDashboardDto.builder()
                    .totalUsers(totalUsers)
                    .activeUsers(activeUsers)
                    .totalPatients(totalPatients)
                    .totalDoctors(totalDoctors)
                    .todayAppointments(todayAppointments)
                    .pendingApprovals(pendingApprovals)
                    .systemHealth(SystemHealthStatus.HEALTHY)
                    .recentActivities(recentActivities)
                    .statistics(statistics)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching dashboard data", e);
            throw new RuntimeException("Failed to fetch dashboard data: " + e.getMessage());
        }
    }
    @Override
    @Transactional(readOnly = true)
    public SystemStatsDto getSystemStatistics() {
        log.info("Fetching system statistics");

        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByEnabledTrue();
            long inactiveUsers = userRepository.countByEnabledFalse();
            long lockedUsers = userRepository.countByAccountLockedTrue();

            // Get user counts by role
            Map<String, Long> usersByRole = new HashMap<>();
            for (Role.RoleName roleName : Role.RoleName.values()) {
                long count = userRepository.countByRoles_Name(roleName);
                usersByRole.put(roleName.name(), count);
            }

            return SystemStatsDto.builder()
                    .totalUsers(totalUsers)
                    .activeUsers(activeUsers)
                    .inactiveUsers(inactiveUsers)
                    .lockedUsers(lockedUsers)
                    .usersByRole(usersByRole)
                    .totalDepartments(departmentRepository.count())
                    .totalRoles(roleRepository.count())
                    .totalPermissions(permissionRepository.count())
                    .systemUptime(getSystemUptime())
                    .lastUpdated(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching system statistics", e);
            throw new RuntimeException("Failed to fetch system statistics: " + e.getMessage());
        }
    }



    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsersWithFilters(String search, Role.RoleName role,
                                                Boolean enabled, Boolean locked, Pageable pageable) {
        log.debug("Fetching users with filters - search: {}, role: {}, enabled: {}, locked: {}",
                search, role, enabled, locked);

        try {
            Specification<User> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Search filter (username, email, first name, last name)
                if (search != null && !search.trim().isEmpty()) {
                    String searchTerm = "%" + search.toLowerCase() + "%";
                    Predicate searchPredicate = criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchTerm),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchTerm),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchTerm),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchTerm)
                    );
                    predicates.add(searchPredicate);
                }

                // Role filter
                if (role != null) {
                    predicates.add(criteriaBuilder.equal(root.join("roles").get("name"), role));
                }

                // Enabled filter
                if (enabled != null) {
                    predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
                }

                // Locked filter
                if (locked != null) {
                    predicates.add(criteriaBuilder.equal(root.get("accountLocked"), locked));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            Page<User> users = userRepository.findAll(spec, pageable);
            return users.map(userMapper::toDto);

        } catch (Exception e) {
            log.error("Error fetching users with filters", e);
            throw new RuntimeException("Failed to fetch users: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public BulkOperationResultDto bulkCreateUsers(MultipartFile file) {
        log.info("Processing bulk user creation from file: {}", file.getOriginalFilename());

        try {
            List<UserRegistrationDto> users = parseUsersFromCsvFile(file);
            int successCount = 0;
            int failureCount = 0;
            List<String> errors = new ArrayList<>();

            for (UserRegistrationDto userDto : users) {
                try {
                    // Set default password if not provided
                    if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
                        userDto.setPassword("TempPass123!");
                    }

                    userService.registerUser(userDto);
                    successCount++;
                    log.debug("Successfully created user: {}", userDto.getUsername());

                } catch (Exception e) {
                    failureCount++;
                    String error = String.format("Failed to create user '%s': %s",
                            userDto.getUsername(), e.getMessage());
                    errors.add(error);
                    log.warn("User creation failed: {}", error);
                }
            }

            String message = String.format("Bulk creation completed: %d successful, %d failed out of %d total",
                    successCount, failureCount, users.size());

            return BulkOperationResultDto.builder()
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .errors(errors)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.error("Bulk user creation failed", e);
            return BulkOperationResultDto.builder()
                    .successCount(0)
                    .failureCount(0)
                    .errors(List.of("File processing failed: " + e.getMessage()))
                    .message("Bulk creation failed")
                    .build();
        }
    }


    @Override
    @Transactional
    public BulkOperationResultDto performBulkUserAction(BulkUserActionDto actionDto) {
        log.info("Performing bulk action: {} on {} users", actionDto.getAction(), actionDto.getUserIds().size());

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long userId : actionDto.getUserIds()) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId));

                switch (actionDto.getAction().toUpperCase()) {
                    case "ACTIVATE":
                        user.setEnabled(true);
                        userRepository.save(user);
                        log.debug("Activated user: {}", userId);
                        break;

                    case "DEACTIVATE":
                        user.setEnabled(false);
                        userRepository.save(user);
                        log.debug("Deactivated user: {}", userId);
                        break;

                    case "LOCK":
                        user.setAccountLocked(true);
                        user.setLockTime(LocalDateTime.now());
                        userRepository.save(user);
                        log.debug("Locked user: {}", userId);
                        break;

                    case "UNLOCK":
                        user.setAccountLocked(false);
                        user.setLockTime(null);
                        user.setFailedLoginAttempts(0);
                        userRepository.save(user);
                        log.debug("Unlocked user: {}", userId);
                        break;

                    case "DELETE":
                        userRepository.delete(user);
                        log.debug("Deleted user: {}", userId);
                        break;

                    default:
                        throw new IllegalArgumentException("Unknown action: " + actionDto.getAction());
                }

                successCount++;

            } catch (Exception e) {
                failureCount++;
                String error = String.format("Failed to %s user %d: %s",
                        actionDto.getAction().toLowerCase(), userId, e.getMessage());
                errors.add(error);
                log.warn("Bulk action failed for user {}: {}", userId, e.getMessage());
            }
        }

        String message = String.format("Bulk %s completed: %d successful, %d failed",
                actionDto.getAction().toLowerCase(), successCount, failureCount);

        return BulkOperationResultDto.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .message(message)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        log.debug("Fetching all roles");

        try {
            return roleRepository.findAll().stream()
                    .map(role -> {
                        RoleDto dto = roleMapper.toDto(role);
                        // Add user count for each role
                        dto.setUserCount(userRepository.countByRoles_Name(role.getName()));
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching roles", e);
            throw new RuntimeException("Failed to fetch roles: " + e.getMessage());
        }
    }




    @Override
    public RoleDto updateRolePermissions(Long roleId, UpdateRolePermissionsDto permissionsDto) {
        return null;
    }

    @Override
    @Transactional
    public RoleDto createRole(CreateRoleDto roleDto) {
        log.info("Creating new role: {}", roleDto.getName());

        try {
            // Check if role already exists
            Role.RoleName roleName = Role.RoleName.valueOf(roleDto.getName().toUpperCase());
            if (roleRepository.existsByName(roleName)) {
                throw new IllegalArgumentException("Role already exists: " + roleDto.getName());
            }

            Role role = Role.builder()
                    .name(roleName)
                    .description(roleDto.getDescription())
                    .isActive(true)
                    .isSystemRole(false)
                    .permissions(new HashSet<>())
                    .build();

            // Add permissions if provided
            if (roleDto.getPermissionIds() != null && !roleDto.getPermissionIds().isEmpty()) {
                Set<Permission> permissions = new HashSet<>(
                        permissionRepository.findAllById(roleDto.getPermissionIds()));
                role.setPermissions(permissions);
            }

            Role savedRole = roleRepository.save(role);
            log.info("Role created successfully: {}", savedRole.getName());

            return roleMapper.toDto(savedRole);

        } catch (IllegalArgumentException e) {
            log.error("Invalid role name: {}", roleDto.getName(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error creating role: {}", roleDto.getName(), e);
            throw new RuntimeException("Failed to create role: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions(String module, String category) {
        log.debug("Fetching permissions - module: {}, category: {}", module, category);

        try {
            List<Permission> permissions;

            if (module != null && category != null) {
                permissions = permissionRepository.findByModuleAndCategory(module, category);
            } else if (module != null) {
                permissions = permissionRepository.findByModule(module);
            } else if (category != null) {
                permissions = permissionRepository.findByCategory(category);
            } else {
                permissions = permissionRepository.findByIsActiveTrue();
            }

            return permissions.stream()
                    .map(permissionMapper::toDto)
                    .sorted(Comparator.comparing(PermissionDto::getModule)
                            .thenComparing(PermissionDto::getCategory)
                            .thenComparing(PermissionDto::getName))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching permissions", e);
            throw new RuntimeException("Failed to fetch permissions: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        log.debug("Fetching all departments");

        try {
            return departmentRepository.findAll().stream()
                    .map(departmentMapper::toDto)
                    .sorted(Comparator.comparing(DepartmentDto::getName))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching departments", e);
            throw new RuntimeException("Failed to fetch departments: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentDto departmentDto) {
        log.info("Creating department: {}", departmentDto.getName());

        try {
            // Check if department already exists
            if (departmentRepository.existsByName(departmentDto.getName())) {
                throw new IllegalArgumentException("Department already exists: " + departmentDto.getName());
            }

            Department department = Department.builder()
                    .name(departmentDto.getName())
                    .description(departmentDto.getDescription())
                    .location(departmentDto.getLocation())
                    .phone(departmentDto.getPhone())
                    .email(departmentDto.getEmail())
                    .active(true)
                    .build();

            Department savedDepartment = departmentRepository.save(department);
            log.info("Department created successfully: {}", savedDepartment.getName());

            return departmentMapper.toDto(savedDepartment);

        } catch (Exception e) {
            log.error("Error creating department: {}", departmentDto.getName(), e);
            throw new RuntimeException("Failed to create department: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public DepartmentDto updateDepartment(Long id, UpdateDepartmentDto updateDto) {
        log.info("Updating department: {}", id);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));

            // Check if name is being changed and if new name already exists
            if (!department.getName().equals(updateDto.getName()) &&
                    departmentRepository.existsByName(updateDto.getName())) {
                throw new IllegalArgumentException("Department name already exists: " + updateDto.getName());
            }

            department.setName(updateDto.getName());
            department.setDescription(updateDto.getDescription());
            department.setLocation(updateDto.getLocation());
            department.setPhone(updateDto.getPhone());
            department.setEmail(updateDto.getEmail());

            Department savedDepartment = departmentRepository.save(department);
            log.info("Department updated successfully: {}", savedDepartment.getName());

            return departmentMapper.toDto(savedDepartment);

        } catch (Exception e) {
            log.error("Error updating department: {}", id, e);
            throw new RuntimeException("Failed to update department: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.warn("Deleting department: {}", id);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));

            // Check if department has associated users/doctors (implement based on your entities)
            // if (hasDependentRecords(id)) {
            //     throw new IllegalArgumentException("Cannot delete department with associated records");
            // }

            departmentRepository.delete(department);
            log.info("Department deleted successfully: {}", department.getName());

        } catch (Exception e) {
            log.error("Error deleting department: {}", id, e);
            throw new RuntimeException("Failed to delete department: " + e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public UserActivityReportDto getUserActivityReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating user activity report for period: {} to {}", startDate, endDate);

        try {
            // Get audit logs for the period
            List<AuditLog> auditLogs = auditLogRepository.findByTimestampBetween(startDate, endDate);

            // Calculate statistics
            long totalLogins = auditLogs.stream()
                    .filter(log -> "LOGIN_SUCCESS".equals(log.getAction()))
                    .count();

            long uniqueUsers = auditLogs.stream()
                    .filter(log -> log.getUserId() != null)
                    .map(AuditLog::getUserId)
                    .collect(Collectors.toSet())
                    .size();

            // Activity by hour
            Map<Integer, Long> activityByHour = auditLogs.stream()
                    .collect(Collectors.groupingBy(
                            log -> log.getTimestamp().getHour(),
                            Collectors.counting()
                    ));

            // Top active users (simplified)
            List<UserActivitySummaryDto> topActiveUsers = auditLogs.stream()
                    .filter(log -> log.getUserId() != null)
                    .collect(Collectors.groupingBy(AuditLog::getUserId, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Optional<User> user = userRepository.findById(entry.getKey());
                        return UserActivitySummaryDto.builder()
                                .userId(entry.getKey())
                                .username(user.map(User::getUsername).orElse("Unknown"))
                                .fullName(user.map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown"))
                                .actionCount(entry.getValue())
                                .build();
                    })
                    .collect(Collectors.toList());

            return UserActivityReportDto.builder()
                    .reportPeriod(startDate + " to " + endDate)
                    .totalLogins(totalLogins)
                    .uniqueUsers(uniqueUsers)
                    .averageSessionDuration(30.0) // Placeholder
                    .topActiveUsers(topActiveUsers)
                    .activityByHour(activityByHour)
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error generating user activity report", e);
            throw new RuntimeException("Failed to generate user activity report: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SystemUsageReportDto getSystemUsageReport(int days) {
        log.info("Generating system usage report for last {} days", days);

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            List<AuditLog> auditLogs = auditLogRepository.findByTimestampAfter(startDate);

            long totalRequests = auditLogs.size();
            double errorRate = totalRequests > 0 ?
                    auditLogs.stream()
                            .filter(log -> log.getAction().contains("ERROR") || log.getAction().contains("FAILED"))
                            .count() * 100.0 / totalRequests : 0.0;

            // Fix: Create proper List<EndpointUsageDto>
            List<EndpointUsageDto> popularEndpoints = auditLogs.stream()
                    .filter(log -> log.getRequestUrl() != null)
                    .collect(Collectors.groupingBy(AuditLog::getRequestUrl, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> EndpointUsageDto.builder()
                            .endpoint(entry.getKey())
                            .requestCount(entry.getValue())
                            .averageResponseTime(150.0) // Placeholder
                            .errorRate(0.0) // Placeholder
                            .build())
                    .collect(Collectors.toList());

            return SystemUsageReportDto.builder()
                    .reportPeriod(days + " days")
                    .totalRequests(totalRequests)
                    .averageResponseTime(150.0) // Placeholder
                    .errorRate(errorRate)
                    .peakUsageHour(14) // Placeholder
                    .popularEndpoints(popularEndpoints)
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error generating system usage report", e);
            throw new RuntimeException("Failed to generate system usage report: " + e.getMessage());
        }
    }
    @Override
    @Transactional(readOnly = true)
    public String exportAuditLogs(AuditLogExportDto exportDto) {
        log.info("Exporting audit logs for period: {} to {}", exportDto.getStartDate(), exportDto.getEndDate());

        try {
            // Generate export file (simplified implementation)
            String exportId = "export-" + System.currentTimeMillis();
            String filePath = "/exports/audit-logs-" + exportId + ".csv";

            // In a real implementation, you would:
            // 1. Query audit logs based on exportDto filters
            // 2. Generate the file in the requested format
            // 3. Store it in a temporary location
            // 4. Return the download URL

            log.info("Audit logs exported successfully: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error exporting audit logs", e);
            throw new RuntimeException("Failed to export audit logs: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String emergencyPasswordReset(Long userId) {
        log.warn("Emergency password reset for user: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            String tempPassword = generateSecureTemporaryPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setPasswordChangedDate(LocalDateTime.now());
            user.setMustChangePassword(true);
            user.setEnabled(true); // Ensure account is enabled
            user.setAccountLocked(false); // Unlock if locked
            user.setFailedLoginAttempts(0); // Reset failed attempts

            userRepository.save(user);
            log.info("Emergency password reset completed for user: {}", user.getUsername());

            return tempPassword;

        } catch (Exception e) {
            log.error("Error during emergency password reset for user: {}", userId, e);
            throw new RuntimeException("Failed to reset password: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public int emergencyUnlockAllAccounts() {
        log.warn("Emergency unlock all locked accounts");

        try {
            List<User> lockedUsers = userRepository.findByAccountLockedTrue();

            for (User user : lockedUsers) {
                user.setAccountLocked(false);
                user.setLockTime(null);
                user.setFailedLoginAttempts(0);
            }

            if (!lockedUsers.isEmpty()) {
                userRepository.saveAll(lockedUsers);
            }

            log.info("Emergency unlock completed for {} accounts", lockedUsers.size());
            return lockedUsers.size();

        } catch (Exception e) {
            log.error("Error during emergency unlock all accounts", e);
            throw new RuntimeException("Failed to unlock accounts: " + e.getMessage());
        }
    }



    @Override
    @Transactional
    public int sendBroadcastNotification(BroadcastNotificationDto notificationDto) {
        log.info("Sending broadcast notification: {}", notificationDto.getTitle());

        try {
            List<User> recipients = userRepository.findByEnabledTrue();

            // Filter by roles if specified
            if (notificationDto.getTargetRoles() != null && !notificationDto.getTargetRoles().isEmpty()) {
                recipients = recipients.stream()
                        .filter(user -> user.getRoles().stream()
                                .anyMatch(role -> notificationDto.getTargetRoles().contains(role.getName())))
                        .collect(Collectors.toList());
            }

            // In a real implementation, you would:
            // 1. Create notification records in database
            // 2. Send push notifications
            // 3. Send emails if configured
            // 4. Update user notification preferences

            log.info("Broadcast notification sent to {} recipients", recipients.size());
            return recipients.size();

        } catch (Exception e) {
            log.error("Error sending broadcast notification", e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage());
        }
    }


    private void validateAdminRegistration(AdminRegistrationDto dto) {
        if (adminRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }
        if (dto.getExtensionNumber() != null && adminRepository.existsByExtensionNumber(dto.getExtensionNumber())) {
            throw new UserAlreadyExistsException("extension number", dto.getExtensionNumber());
        }
    }

    private void validateAdminUpdate(AdminRegistrationDto dto, Long adminId) {
        if (!isEmployeeIdAvailable(dto.getEmployeeId(), adminId)) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }
        if (dto.getExtensionNumber() != null && !isExtensionNumberAvailable(dto.getExtensionNumber(), adminId)) {
            throw new UserAlreadyExistsException("extension number", dto.getExtensionNumber());
        }
    }

    // Helper method for updating boolean permissions in repository
    @FunctionalInterface
    private interface PermissionUpdater {
        void update(Long id, boolean value);
    }

    private void updateBooleanPermission(Long adminId, boolean value, String permissionName, PermissionUpdater updater) {
        log.info("Updating permission {} for admin {} to {}", permissionName, adminId, value);
        adminRepository.findById(adminId).orElseThrow(() -> new UserNotFoundException("Admin not found with ID: " + adminId));
        updater.update(adminId, value);
        auditService.logUserAction(permissionName.toUpperCase() + "_UPDATED", adminId,
                permissionName + " permission updated to: " + value);
    }



    // Helper Methods

    private Long getTodayAppointmentsCount() {
        // Implement based on your appointment entity
        // return appointmentRepository.countByAppointmentDateBetween(startOfDay, endOfDay);
        return 0L;
    }

    private Long getPendingApprovalsCount() {
        // Implement based on your business logic
        return 0L;
    }

    private List<RecentActivityDto> getRecentActivities() {
        try {
            return auditLogRepository.findTop10ByOrderByTimestampDesc()
                    .stream()
                    .map(auditLog -> RecentActivityDto.builder()
                            .id(auditLog.getId())
                            .action(auditLog.getAction())
                            .description(auditLog.getDetails())
                            .username(auditLog.getUsername())
                            .timestamp(auditLog.getTimestamp())
                            .ipAddress(auditLog.getIpAddress())
                            .severity(auditLog.getSeverity() != null ? auditLog.getSeverity().toString() : "INFO")
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch recent activities", e);
            return new ArrayList<>();
        }
    }

    private String getSystemUptime() {
        // Simple placeholder implementation
        long uptimeMillis = System.currentTimeMillis() - getApplicationStartTime();
        long hours = uptimeMillis / (1000 * 60 * 60);
        long minutes = (uptimeMillis % (1000 * 60 * 60)) / (1000 * 60);
        return String.format("%02d:%02d:00", hours, minutes);
    }

    private long getApplicationStartTime() {
        // This should be set when application starts
        // For now, return a placeholder
        return System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 hours ago
    }

    private List<UserRegistrationDto> parseUsersFromCsvFile(MultipartFile file) throws Exception {
        List<UserRegistrationDto> users = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : parser) {
                UserRegistrationDto user = UserRegistrationDto.builder()
                        .username(record.get("username"))
                        .email(record.get("email"))
                        .firstName(record.get("firstName"))
                        .lastName(record.get("lastName"))
                        .phone(record.get("phone"))
                        .password(record.isSet("password") ? record.get("password") : "TempPass123!")
                        .role(Role.RoleName.valueOf(record.get("role").toUpperCase()))
                        .build();

                users.add(user);
            }
        }

        return users;
    }

    private String generateSecureTemporaryPassword() {
        // Generate a secure temporary password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}





