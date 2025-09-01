package com.hospital_management.mapper;


import com.hospital_management.dtos.NurseDto;
import com.hospital_management.dtos.NurseRegistrationDto;
import com.hospital_management.models.Nurse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface NurseMapper {


    NurseDto toDto(Nurse nurse);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isAvailableForDuty", constant = "true")
    @Mapping(target = "isHeadNurse", constant = "false")
    @Mapping(target = "canAdministerMedication", constant = "false")
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
    Nurse toEntity(NurseRegistrationDto dto);

    List<NurseDto> toDtoList(List<Nurse> nurses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateNurseFromDto(NurseRegistrationDto dto, @MappingTarget Nurse nurse);
}
