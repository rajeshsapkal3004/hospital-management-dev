package com.hospital_management.mapper;


import com.hospital_management.dtos.AdminDto;
import com.hospital_management.dtos.AdminRegistrationDto;
import com.hospital_management.models.Admin;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AdminMapper {


    AdminDto toDto(Admin admin);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "canManageUsers", source = "canManageUsers")
    @Mapping(target = "canAccessFinancialData", source = "canAccessFinancialData")
    @Mapping(target = "canGenerateReports", source = "canGenerateReports")
    @Mapping(target = "canModifySystemSettings", source = "canModifySystemSettings")
    @Mapping(target = "isSuperAdmin", source = "isSuperAdmin")
    @Mapping(target = "managedDepartments", source = "managedDepartments")
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
    Admin toEntity(AdminRegistrationDto dto);

    List<AdminDto> toDtoList(List<Admin> admins);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateAdminFromDto(AdminRegistrationDto dto, @MappingTarget Admin admin);
}
