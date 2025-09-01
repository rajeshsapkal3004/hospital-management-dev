package com.hospital_management.services.userservice;

import com.hospital_management.dtos.PermissionCreateDto;
import com.hospital_management.dtos.PermissionDto;
import com.hospital_management.exception.UserAlreadyExistsException;
import com.hospital_management.exception.UserNotFoundException;
import com.hospital_management.exception.ValidationException;
import com.hospital_management.mapper.PermissionMapper;
import com.hospital_management.models.Permission;
import com.hospital_management.models.Role;
import com.hospital_management.repo.PermissionRepository;
import com.hospital_management.services.auditlogs.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Validated
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionDto createPermission(@Valid PermissionCreateDto createDto) {
        log.info("Creating new permission: {}", createDto.getName());

        if (permissionRepository.existsByName(createDto.getName())) {
            throw new UserAlreadyExistsException("Permission already exists: " + createDto.getName());
        }

        if (permissionRepository.existsByResourceAndAction(createDto.getResource(), createDto.getAction())) {
            throw new UserAlreadyExistsException("Permission already exists for resource: " +
                    createDto.getResource() + " and action: " + createDto.getAction());
        }

        try {
            Permission permission = Permission.builder()
                    .name(createDto.getName())
                    .description(createDto.getDescription())
                    .resource(createDto.getResource())
                    .action(createDto.getAction())
                    .permissionType(createDto.getPermissionType() != null ?
                            createDto.getPermissionType() : Permission.PermissionType.FUNCTIONAL)
                    .resourcePath(createDto.getResourcePath())
                    .priority(createDto.getPriority())
                    .module(createDto.getModule())
                    .category(createDto.getCategory())
                    .requiresApproval(createDto.getRequiresApproval())
                    .conditions(createDto.getConditions())
                    .isActive(createDto.getIsActive())
                    .isSystemPermission(createDto.getIsSystemPermission())
                    .build();

            Permission savedPermission = permissionRepository.save(permission);

            auditService.logUserAction("PERMISSION_CREATED", null,
                    "Permission created: " + savedPermission.getName());

            log.info("Permission created successfully with ID: {}", savedPermission.getId());
            return permissionMapper.toDto(savedPermission);

        } catch (Exception e) {
            log.error("Error creating permission: {}", e.getMessage(), e);
            throw new ValidationException("Failed to create permission: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permissions", key = "#id")
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionDto getPermissionById(Long id) {
        log.debug("Fetching permission by ID: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Permission not found with ID: " + id));
        return permissionMapper.toDto(permission);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permissions", key = "#name")
    public PermissionDto getPermissionByName(String name) {
        log.debug("Fetching permission by name: {}", name);
        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new UserNotFoundException("Permission not found with name: " + name));
        return permissionMapper.toDto(permission);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "all-permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PermissionDto> getAllPermissions() {
        log.debug("Fetching all permissions");
        return permissionMapper.toDtoList(permissionRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "active-permissions")
    public List<PermissionDto> getActivePermissions() {
        log.debug("Fetching active permissions");
        return permissionMapper.toDtoList(permissionRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"permissions", "all-permissions", "active-permissions"}, allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionDto updatePermission(Long id, @Valid PermissionCreateDto updateDto) {
        log.info("Updating permission with ID: {}", id);

        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Permission not found with ID: " + id));

        // Validate unique constraints
        if (!existingPermission.getName().equals(updateDto.getName()) &&
                permissionRepository.existsByName(updateDto.getName())) {
            throw new UserAlreadyExistsException("Permission name already exists: " + updateDto.getName());
        }

        existingPermission.setName(updateDto.getName());
        existingPermission.setDescription(updateDto.getDescription());
        existingPermission.setResource(updateDto.getResource());
        existingPermission.setAction(updateDto.getAction());
        existingPermission.setPermissionType(updateDto.getPermissionType());
        existingPermission.setResourcePath(updateDto.getResourcePath());
        existingPermission.setPriority(updateDto.getPriority());
        existingPermission.setModule(updateDto.getModule());
        existingPermission.setCategory(updateDto.getCategory());
        existingPermission.setRequiresApproval(updateDto.getRequiresApproval());
        existingPermission.setConditions(updateDto.getConditions());
        existingPermission.setIsActive(updateDto.getIsActive());

        Permission savedPermission = permissionRepository.save(existingPermission);

        auditService.logUserAction("PERMISSION_UPDATED", null,
                "Permission updated: " + savedPermission.getName());

        log.info("Permission updated successfully: {}", id);
        return permissionMapper.toDto(savedPermission);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updatePermissionDescription(Long permissionId, String description) {
        log.info("Updating permission description for ID: {}", permissionId);

        permissionRepository.findById(permissionId)
                .orElseThrow(() -> new UserNotFoundException("Permission not found with ID: " + permissionId));

        permissionRepository.updateDescription(permissionId, description);

        auditService.logUserAction("PERMISSION_DESCRIPTION_UPDATED", null,
                "Permission description updated for ID: " + permissionId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updatePermissionPriority(Long permissionId, Integer priority) {
        log.info("Updating permission priority for ID: {}", permissionId);

        if (priority < 1 || priority > 10) {
            throw new ValidationException("Priority must be between 1 and 10");
        }

        permissionRepository.findById(permissionId)
                .orElseThrow(() -> new UserNotFoundException("Permission not found with ID: " + permissionId));

        permissionRepository.updatePriority(permissionId, priority);

        auditService.logUserAction("PERMISSION_PRIORITY_UPDATED", null,
                "Permission priority updated for ID: " + permissionId + " to: " + priority);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void activatePermission(Long permissionId) {
        log.info("Activating permission: {}", permissionId);

        permissionRepository.findById(permissionId)
                .orElseThrow(() -> new UserNotFoundException("Permission not found with ID: " + permissionId));

        permissionRepository.updateActiveStatus(permissionId, true);

        auditService.logUserAction("PERMISSION_ACTIVATED", null,
                "Permission activated: " + permissionId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivatePermission(Long permissionId) {
        log.info("Deactivating permission: {}", permissionId);

        permissionRepository.findById(permissionId)
                .orElseThrow(() -> new UserNotFoundException("Permission not found with ID: " + permissionId));

        permissionRepository.updateActiveStatus(permissionId, false);

        auditService.logUserAction("PERMISSION_DEACTIVATED", null,
                "Permission deactivated: " + permissionId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permissions-by-resource", key = "#resource")
    public List<PermissionDto> getPermissionsByResource(String resource) {
        log.debug("Fetching permissions by resource: {}", resource);
        return permissionMapper.toDtoList(permissionRepository.findByResource(resource));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permissions-by-action", key = "#action")
    public List<PermissionDto> getPermissionsByAction(String action) {
        log.debug("Fetching permissions by action: {}", action);
        return permissionMapper.toDtoList(permissionRepository.findByAction(action));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permissions-by-module", key = "#module")
    public List<PermissionDto> getPermissionsByModule(String module) {
        log.debug("Fetching permissions by module: {}", module);
        return permissionMapper.toDtoList(permissionRepository.findByModule(module));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permissions-by-category", key = "#category")
    public List<PermissionDto> getPermissionsByCategory(String category) {
        log.debug("Fetching permissions by category: {}", category);
        return permissionMapper.toDtoList(permissionRepository.findByCategory(category));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PermissionDto> searchPermissions(String name, String resource, String action,
                                                 String module, String category, Pageable pageable) {
        log.debug("Searching permissions with filters");
        return permissionRepository.findPermissionsWithFilters(name, resource, action, module, category, pageable)
                .map(permissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "all-resources")
    public List<String> getAllResources() {
        log.debug("Fetching all resources");
        return permissionRepository.findAllActiveResources();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "all-actions")
    public List<String> getAllActions() {
        log.debug("Fetching all actions");
        return List.of(Permission.Actions.class.getDeclaredFields())
                .stream()
                .filter(field -> field.getType() == String.class)
                .map(field -> {
                    try {
                        return (String) field.get(null);
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "all-modules")
    public List<String> getAllModules() {
        log.debug("Fetching all modules");
        return permissionRepository.findAllActiveModules();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "all-categories")
    public List<String> getAllCategories() {
        log.debug("Fetching all categories");
        return permissionRepository.findAllActiveCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActionsByResource(String resource) {
        log.debug("Fetching actions by resource: {}", resource);
        return permissionRepository.findActionsByResource(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsByRole(Long roleId) {
        log.debug("Fetching permissions by role ID: {}", roleId);
        return permissionMapper.toDtoList(permissionRepository.findPermissionsByRoleId(roleId));
    }



    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsByRoleName(Role.RoleName roleName) {
        log.debug("Fetching permissions by role name: {}", roleName);
        return permissionMapper.toDtoList(permissionRepository.findPermissionsByRoleName(roleName.name()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByResourceAndAction(String resource, String action) {
        return permissionRepository.existsByResourceAndAction(resource, action);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalPermissionCount() {
        return permissionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActivePermissionCount() {
        return permissionRepository.countByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permission-statistics", key = "'resource-counts'")
    public Map<String, Long> getPermissionCountByResource() {
        List<Object[]> results = permissionRepository.getPermissionUsageByActiveRoles();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permission-statistics", key = "'module-counts'")
    public Map<String, Long> getPermissionCountByModule() {
        List<Object[]> results = permissionRepository.getPermissionUsageByActiveRoles();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }
}
