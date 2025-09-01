package com.hospital_management.mapper;


import com.hospital_management.dtos.EmergencyContactDto;
import com.hospital_management.models.EmergencyContact;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmergencyContactMapper {

    EmergencyContactMapper INSTANCE = Mappers.getMapper(EmergencyContactMapper.class);

    EmergencyContactDto toDto(EmergencyContact emergencyContact);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    EmergencyContact toEntity(EmergencyContactDto dto);
}
