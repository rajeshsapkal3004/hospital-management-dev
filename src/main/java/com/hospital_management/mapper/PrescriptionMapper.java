package com.hospital_management.mapper;



import com.hospital_management.dtos.PrescriptionDto;
import com.hospital_management.models.Prescription;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PrescriptionItemMapper.class})
public interface PrescriptionMapper {

    PrescriptionMapper INSTANCE = Mappers.getMapper(PrescriptionMapper.class);

    @Mapping(target = "patientName", expression = "java(prescription.getPatient().getFirstName() + \" \" + prescription.getPatient().getLastName())")
    @Mapping(target = "patientEmail", source = "patient.email")
    @Mapping(target = "doctorName", expression = "java(prescription.getDoctor().getFirstName() + \" \" + prescription.getDoctor().getLastName())")
    @Mapping(target = "doctorSpecialization", source = "doctor.specialization")
    @Mapping(target = "medicalRecordId", source = "medicalRecord.id")
    @Mapping(target = "statusDisplayName", expression = "java(prescription.getStatus().getDisplayName())")
    @Mapping(target = "priorityDisplayName", expression = "java(prescription.getPriority().getDisplayName())")
    @Mapping(target = "isExpired", expression = "java(prescription.isExpired())")
    @Mapping(target = "canBeDispensed", expression = "java(prescription.canBeDispensed())")
    @Mapping(target = "hasRepeatsAvailable", expression = "java(prescription.hasRepeatsAvailable())")
    PrescriptionDto toDto(Prescription prescription);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "prescriptionItems", ignore = true)
    Prescription toEntity(PrescriptionDto dto);
}
