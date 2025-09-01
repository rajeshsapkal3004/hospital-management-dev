package com.hospital_management.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// UpdateDepartmentDto.java
@Data
public class UpdateDepartmentDto {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String location;

    private String phone;

    @Email
    private String email;
}