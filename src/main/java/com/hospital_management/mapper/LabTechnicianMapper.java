package com.hospital_management.mapper;

import com.hospital_management.dtos.LabTechnicianDto;
import com.hospital_management.dtos.LabTechnicianRegistrationDto;
import com.hospital_management.models.LabTechnician;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface LabTechnicianMapper {


    @Mapping(target = "certificationExpiring", expression = "java(isCertificationExpiring(labTechnician))")
    LabTechnicianDto toDto(LabTechnician labTechnician);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
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
    LabTechnician toEntity(LabTechnicianRegistrationDto dto);

    List<LabTechnicianDto> toDtoList(List<LabTechnician> labTechnicians);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateLabTechnicianFromDto(LabTechnicianRegistrationDto dto, @MappingTarget LabTechnician labTechnician);

    // Helper method for checking certification expiration
    default boolean isCertificationExpiring(LabTechnician labTechnician) {
        if (labTechnician.getCertificationExpiry() == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        LocalDate expiryWarningDate = now.plusDays(30); // Warning 30 days before expiry
        return labTechnician.getCertificationExpiry().isBefore(expiryWarningDate);
    }
}
