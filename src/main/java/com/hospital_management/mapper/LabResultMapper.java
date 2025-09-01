package com.hospital_management.mapper;


import com.hospital_management.dtos.LabResultDto;
import com.hospital_management.models.LabResult;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LabResultMapper {

    LabResultMapper INSTANCE = Mappers.getMapper(LabResultMapper.class);

    @Mapping(target = "patientName", expression = "java(labResult.getPatient().getFirstName() + \" \" + labResult.getPatient().getLastName())")
    @Mapping(target = "labTestId", source = "labTest.id")
    @Mapping(target = "statusDisplayName", expression = "java(labResult.getStatus().getDisplayName())")
    @Mapping(target = "resultFlagDisplayName", expression = "java(labResult.getResultFlag() != null ? labResult.getResultFlag().getDisplayName() : null)")
    @Mapping(target = "isCompleted", expression = "java(labResult.isCompleted())")
    @Mapping(target = "requiresAttention", expression = "java(labResult.requiresAttention())")
    LabResultDto toDto(LabResult labResult);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "labTest", ignore = true)
    LabResult toEntity(LabResultDto dto);
}
