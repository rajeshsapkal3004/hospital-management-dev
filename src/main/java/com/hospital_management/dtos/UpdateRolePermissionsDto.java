package com.hospital_management.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRolePermissionsDto {
    @NotEmpty
    private List<Long> permissionIds;
}
