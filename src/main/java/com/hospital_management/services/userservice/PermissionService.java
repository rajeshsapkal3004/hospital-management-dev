package com.hospital_management.services.userservice;


import com.hospital_management.dtos.PermissionCreateDto;
import com.hospital_management.dtos.PermissionDto;
import com.hospital_management.models.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    // Basic operations
    PermissionDto createPermission(PermissionCreateDto createDto);
    PermissionDto getPermissionById(Long id);
    PermissionDto getPermissionByName(String name);
    List<PermissionDto> getAllPermissions();
    List<PermissionDto> getActivePermissions();

    // Update operations
    PermissionDto updatePermission(Long id, PermissionCreateDto updateDto);
    void updatePermissionDescription(Long permissionId, String description);
    void updatePermissionPriority(Long permissionId, Integer priority);

    // Status management
    void activatePermission(Long permissionId);
    void deactivatePermission(Long permissionId);

    // Search and filtering
    List<PermissionDto> getPermissionsByResource(String resource);
    List<PermissionDto> getPermissionsByAction(String action);
    List<PermissionDto> getPermissionsByModule(String module);
    List<PermissionDto> getPermissionsByCategory(String category);
    Page<PermissionDto> searchPermissions(String name, String resource, String action,
                                          String module, String category, Pageable pageable);

    // Resource and action queries
    List<String> getAllResources();
    List<String> getAllActions();
    List<String> getAllModules();
    List<String> getAllCategories();
    List<String> getActionsByResource(String resource);

    // Role-based queries
    List<PermissionDto> getPermissionsByRole(Long roleId);
    List<PermissionDto> getPermissionsByRoleName(Role.RoleName roleName);

    // Validation
    boolean existsByName(String name);
    boolean existsByResourceAndAction(String resource, String action);

    // Statistics
    long getTotalPermissionCount();
    long getActivePermissionCount();
    Map<String, Long> getPermissionCountByResource();
    Map<String, Long> getPermissionCountByModule();
}
