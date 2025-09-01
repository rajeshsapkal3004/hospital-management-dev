package com.hospital_management.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private boolean accountLocked;
    private boolean enabled;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdDate;
    private Set<RoleDto> roles;
    private Map<String, String> profileData;


}
