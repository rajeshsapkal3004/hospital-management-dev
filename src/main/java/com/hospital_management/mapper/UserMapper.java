package com.hospital_management.mapper;

import com.hospital_management.dtos.UserDto;
import com.hospital_management.dtos.UserRegistrationDto;
import com.hospital_management.models.User;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    // Remove the password mapping here because UserDto has no password field
    @Mapping(target = "roles", source = "roles")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    // No mapping for password here either—entity builder will handle password separately
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "accountLocked", constant = "false")
    @Mapping(target = "accountExpired", constant = "false")
    @Mapping(target = "credentialsExpired", constant = "false")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastLoginDate", ignore = true)
    @Mapping(target = "passwordChangedDate", ignore = true)
    @Mapping(target = "accountLockedDate", ignore = true)
    User toEntity(UserRegistrationDto dto);

    List<UserDto> toDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true) // Username should not be updated
    // No password mapping here either
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateUserFromDto(UserRegistrationDto dto, @MappingTarget User user);
}
