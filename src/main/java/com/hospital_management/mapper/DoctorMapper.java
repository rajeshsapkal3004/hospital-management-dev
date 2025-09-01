package com.hospital_management.mapper;

import com.hospital_management.dtos.DoctorDto;
import com.hospital_management.dtos.DoctorRegistrationDto;
import com.hospital_management.models.Doctor;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface DoctorMapper {

    // No password mapping here; DoctorDto has no password field
    @Mapping(target = "roles", source = "roles")
    DoctorDto toDto(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isAvailableForConsultation", constant = "true")
    @Mapping(target = "isEmergencyContact", constant = "false")
    // Ignore other inherited user fields not set from registration DTO
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
    Doctor toEntity(DoctorRegistrationDto dto);

    List<DoctorDto> toDtoList(List<Doctor> doctors);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "accountExpired", ignore = true)
    @Mapping(target = "credentialsExpired", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "phoneVerified", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastLoginDate", ignore = true)
    @Mapping(target = "passwordChangedDate", ignore = true)
    @Mapping(target = "accountLockedDate", ignore = true)
    void updateDoctorFromDto(DoctorRegistrationDto dto, @MappingTarget Doctor doctor);
}
