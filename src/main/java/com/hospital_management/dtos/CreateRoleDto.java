package com.hospital_management.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

// CreateRoleDto.java
@Data
public class CreateRoleDto {
    @NotBlank
    private String name;

    private String description;

    private List<Long> permissionIds;
}
