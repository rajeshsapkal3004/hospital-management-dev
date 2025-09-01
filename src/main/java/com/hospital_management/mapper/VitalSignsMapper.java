package com.hospital_management.mapper;


import com.hospital_management.dtos.VitalSignsDto;
import com.hospital_management.models.VitalSigns;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VitalSignsMapper {

    VitalSignsMapper INSTANCE = Mappers.getMapper(VitalSignsMapper.class);

    @Mapping(target = "patientName", expression = "java(vitalSigns.getPatient() != null ? vitalSigns.getPatient().getFirstName() + \" \" + vitalSigns.getPatient().getLastName() : null)")
    @Mapping(target = "bloodPressure", expression = "java(vitalSigns.getBloodPressure())")
    VitalSignsDto toDto(VitalSigns vitalSigns);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    VitalSigns toEntity(VitalSignsDto dto);
}
