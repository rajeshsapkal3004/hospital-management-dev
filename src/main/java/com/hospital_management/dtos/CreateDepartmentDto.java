package com.hospital_management.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// CreateDepartmentDto.java
@Data
public class CreateDepartmentDto {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String location;

    private String phone;

    @Email
    private String email;
}