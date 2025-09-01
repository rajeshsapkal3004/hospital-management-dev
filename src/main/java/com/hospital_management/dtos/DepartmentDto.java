package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DepartmentDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDto {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String phone;
    private String email;
    private Boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}

