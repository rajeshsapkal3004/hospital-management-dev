package com.hospital_management.dtos;


import com.hospital_management.models.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class RoleCreateDto {

    @NotNull(message = "Role name is required")
    private Role.RoleName name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Min(value = 1, message = "Hierarchy level must be at least 1")
    @Max(value = 10, message = "Hierarchy level cannot exceed 10")
    private Integer hierarchyLevel;

    private Boolean isActive = true;
    private Boolean isSystemRole = false;

    // List of permission IDs to assign to this role
    private List<Long> permissionIds;
}
