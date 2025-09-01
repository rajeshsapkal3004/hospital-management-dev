package com.hospital_management.mapper;



import com.hospital_management.dtos.LabTestDto;
import com.hospital_management.models.LabTest;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {LabResultMapper.class})
public interface LabTestMapper {

    LabTestMapper INSTANCE = Mappers.getMapper(LabTestMapper.class);

    @Mapping(target = "patientName", expression = "java(labTest.getPatient().getFirstName() + \" \" + labTest.getPatient().getLastName())")
    @Mapping(target = "doctorName", expression = "java(labTest.getDoctor().getFirstName() + \" \" + labTest.getDoctor().getLastName())")
    @Mapping(target = "statusDisplayName", expression = "java(labTest.getStatus().getDisplayName())")
    @Mapping(target = "urgencyDisplayName", expression = "java(labTest.getUrgency().getDisplayName())")
    @Mapping(target = "canCollectSample", expression = "java(labTest.canCollectSample())")
    @Mapping(target = "isCompleted", expression = "java(labTest.isCompleted())")
    @Mapping(target = "results", source = "labResults")
    LabTestDto toDto(LabTest labTest);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "labResults", ignore = true)
    LabTest toEntity(LabTestDto dto);
}
