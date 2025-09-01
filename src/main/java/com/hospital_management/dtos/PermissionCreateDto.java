package com.hospital_management.dtos;


import com.hospital_management.models.Permission;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PermissionCreateDto {

    @NotBlank(message = "Permission name is required")
    @Size(min = 3, max = 100, message = "Permission name must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Permission name must contain only uppercase letters and underscores")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotBlank(message = "Resource is required")
    @Size(min = 2, max = 50, message = "Resource name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Resource name must contain only uppercase letters and underscores")
    private String resource;

    @NotBlank(message = "Action is required")
    @Size(min = 2, max = 20, message = "Action name must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Action name must contain only uppercase letters and underscores")
    private String action;

    private Permission.PermissionType permissionType;

    @Size(max = 200, message = "Resource path cannot exceed 200 characters")
    private String resourcePath;

    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority cannot exceed 10")
    private Integer priority = 5;

    @Size(max = 50, message = "Module cannot exceed 50 characters")
    private String module;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    private Boolean requiresApproval = false;

    @Size(max = 500, message = "Conditions cannot exceed 500 characters")
    private String conditions;

    private Boolean isActive = true;
    private Boolean isSystemPermission = false;
}

