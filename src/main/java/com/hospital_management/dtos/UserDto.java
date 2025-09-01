package com.hospital_management.dtos;

import com.hospital_management.models.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private User.Gender gender;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private boolean accountLocked;
    private boolean accountExpired;
    private boolean credentialsExpired;
    private boolean enabled;
    private boolean emailVerified;
    private boolean phoneVerified;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Set<RoleDto> roles;
}
