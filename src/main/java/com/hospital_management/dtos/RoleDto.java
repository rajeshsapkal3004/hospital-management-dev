package com.hospital_management.dtos;

import com.hospital_management.models.Role;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RoleDto {
    private Long id;
    private Role.RoleName name;
    private String description;
    private boolean isActive;
    private boolean isSystemRole;
    private Integer hierarchyLevel;
    private LocalDateTime createdDate;
    private Set<PermissionDto> permissions;
    private Long userCount;
}

