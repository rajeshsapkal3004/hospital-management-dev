package com.hospital_management.dtos;


import com.hospital_management.models.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDto {
    private Long id;
    private String name;
    private String description;
    private String resource;
    private String action;
    private Permission.PermissionType permissionType;
    private boolean isActive;
    private Integer priority;
    private String module;        // Make sure this field exists
    private String category;


}
