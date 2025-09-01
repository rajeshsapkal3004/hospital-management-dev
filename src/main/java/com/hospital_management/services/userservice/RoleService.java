package com.hospital_management.services.userservice;


import com.hospital_management.dtos.RoleCreateDto;
import com.hospital_management.dtos.RoleDto;
import com.hospital_management.models.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface RoleService {

    // Basic operations
    RoleDto createRole(RoleCreateDto createDto);
    RoleDto getRoleById(Long id);
    RoleDto getRoleByName(Role.RoleName name);
    List<RoleDto> getAllRoles();
    List<RoleDto> getActiveRoles();

    // Update operations
    RoleDto updateRole(Long id, RoleCreateDto updateDto);
    void updateRoleDescription(Long roleId, String description);
    void updateHierarchyLevel(Long roleId, Integer level);

    // Status management
    void activateRole(Long roleId);
    void deactivateRole(Long roleId);

    // Permission management
    void assignPermissionToRole(Long roleId, Long permissionId);
    void removePermissionFromRole(Long roleId, Long permissionId);
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    // Search and filtering
    Page<RoleDto> searchRoles(Boolean isActive, Boolean isSystemRole,
                              Integer minHierarchyLevel, Integer maxHierarchyLevel,
                              Pageable pageable);
    List<RoleDto> getRolesByHierarchyLevel(Integer maxLevel);
    List<RoleDto> getRolesWithPermission(Long permissionId);

    // Validation
    boolean existsByName(Role.RoleName name);

    // Statistics
    long getTotalRoleCount();
    long getActiveRoleCount();
    Map<String, Long> getRoleUserCounts();
    Map<String, Long> getRolePermissionCounts();
}
