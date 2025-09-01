package com.hospital_management.services.userservice;

import com.hospital_management.dtos.RoleCreateDto;
import com.hospital_management.dtos.RoleDto;
import com.hospital_management.exception.UserAlreadyExistsException;
import com.hospital_management.exception.UserNotFoundException;
import com.hospital_management.exception.ValidationException;
import com.hospital_management.mapper.RoleMapper;
import com.hospital_management.models.Permission;
import com.hospital_management.models.Role;
import com.hospital_management.repo.PermissionRepository;
import com.hospital_management.repo.RoleRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Validated
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public RoleDto createRole(@Valid RoleCreateDto createDto) {
        log.info("Creating new role: {}", createDto.getName());

        // Validation
        if (roleRepository.existsByName(createDto.getName())) {
            throw new UserAlreadyExistsException("Role already exists: " + createDto.getName());
        }

        try {
            Role role = Role.builder()
                    .name(createDto.getName())
                    .description(createDto.getDescription())
                    .hierarchyLevel(createDto.getHierarchyLevel() != null ?
                            createDto.getHierarchyLevel() : createDto.getName().getDefaultHierarchyLevel())
                    .isActive(createDto.getIsActive())
                    .isSystemRole(createDto.getIsSystemRole())
                    .build();

            // Assign permissions if provided
            if (createDto.getPermissionIds() != null && !createDto.getPermissionIds().isEmpty()) {
                Set<Permission> permissions = permissionRepository.findAllById(createDto.getPermissionIds())
                        .stream().collect(Collectors.toSet());

                for (Permission permission : permissions) {
                    role.addPermission(permission);
                }
            }

            Role savedRole = roleRepository.save(role);

            auditService.logUserAction("ROLE_CREATED", null,
                    "Role created: " + savedRole.getName());

            log.info("Role created successfully with ID: {}", savedRole.getId());
            return roleMapper.toDto(savedRole);

        } catch (Exception e) {
            log.error("Error creating role: {}", e.getMessage(), e);
            throw new ValidationException("Failed to create role: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#id")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleDto getRoleById(Long id) {
        log.debug("Fetching role by ID: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Role not found with ID: " + id));
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#name")
    public RoleDto getRoleByName(Role.RoleName name) {
        log.debug("Fetching role by name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new UserNotFoundException("Role not found with name: " + name));
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "all-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleDto> getAllRoles() {
        log.debug("Fetching all roles");
        return roleMapper.toDtoList(roleRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "active-roles")
    public List<RoleDto> getActiveRoles() {
        log.debug("Fetching active roles");
        return roleMapper.toDtoList(roleRepository.findByIsActiveTrue());
    }

    @Override
    public RoleDto updateRole(Long id, RoleCreateDto updateDto) {
        return null;
    }

    @Override
    public void updateRoleDescription(Long roleId, String description) {

    }

    @Override
    public void updateHierarchyLevel(Long roleId, Integer level) {

    }

    @Override
    public void activateRole(Long roleId) {

    }

    @Override
    public void deactivateRole(Long roleId) {

    }

    @Override
    @Transactional
    @CacheEvict(value = {"roles", "all-roles", "active-roles"}, allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new UserNotFoundException("Role not found with ID: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new UserNotFoundException("Permission not found with ID: " + permissionId));

        role.addPermission(permission);
        roleRepository.save(role);

        auditService.logUserAction("PERMISSION_ASSIGNED_TO_ROLE", null,
                String.format("Permission %s assigned to role %s", permission.getName(), role.getName()));
    }

    @Override
    public void removePermissionFromRole(Long roleId, Long permissionId) {

    }

    @Override
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {

    }

    @Override
    public Page<RoleDto> searchRoles(Boolean isActive, Boolean isSystemRole, Integer minHierarchyLevel, Integer maxHierarchyLevel, Pageable pageable) {
        return null;
    }

    @Override
    public List<RoleDto> getRolesByHierarchyLevel(Integer maxLevel) {
        return null;
    }

    @Override
    public List<RoleDto> getRolesWithPermission(Long permissionId) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(Role.RoleName name) {
        return roleRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalRoleCount() {
        return roleRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveRoleCount() {
        return roleRepository.countByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "role-statistics", key = "'user-counts'")
    public Map<String, Long> getRoleUserCounts() {
        List<Object[]> results = roleRepository.getRoleUserCounts();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            Role.RoleName roleName = (Role.RoleName) result[0];
            Long count = (Long) result[1];
            counts.put(roleName.name(), count);
        }
        return counts;
    }

    @Override
    public Map<String, Long> getRolePermissionCounts() {
        return null;
    }

    // Additional implementation methods following the same pattern...



}

