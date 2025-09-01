package com.hospital_management.mapper;



import com.hospital_management.dtos.MedicalRecordDto;
import com.hospital_management.models.MedicalRecord;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {MedicalRecordAttachmentMapper.class, PrescriptionMapper.class})
public interface MedicalRecordMapper {

    MedicalRecordMapper INSTANCE = Mappers.getMapper(MedicalRecordMapper.class);

    @Mapping(target = "patientName", expression = "java(medicalRecord.getPatient().getFirstName() + \" \" + medicalRecord.getPatient().getLastName())")
    @Mapping(target = "patientEmail", source = "patient.email")
    @Mapping(target = "doctorName", expression = "java(medicalRecord.getDoctor().getFirstName() + \" \" + medicalRecord.getDoctor().getLastName())")
    @Mapping(target = "doctorSpecialization", source = "doctor.specialization")
    @Mapping(target = "recordStatusDisplayName", expression = "java(medicalRecord.getRecordStatus().getDisplayName())")
    @Mapping(target = "confidentialityDisplayName", expression = "java(medicalRecord.getConfidentialityLevel().getDisplayName())")
    @Mapping(target = "canBeModified", expression = "java(medicalRecord.canBeModified())")
    @Mapping(target = "isSigned", expression = "java(medicalRecord.isSigned())")
    @Mapping(target = "bloodPressure", expression = "java(getBloodPressure(medicalRecord.getBloodPressureSystolic(), medicalRecord.getBloodPressureDiastolic()))")
    MedicalRecordDto toDto(MedicalRecord medicalRecord);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "prescriptions", ignore = true)
    MedicalRecord toEntity(MedicalRecordDto dto);

    default String getBloodPressure(Integer systolic, Integer diastolic) {
        if (systolic != null && diastolic != null) {
            return systolic + "/" + diastolic;
        }
        return null;
    }
}
