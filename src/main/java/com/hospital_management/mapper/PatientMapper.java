package com.hospital_management.mapper;


import com.hospital_management.dtos.PatientDto;
import com.hospital_management.dtos.PatientRegistrationDto;
import com.hospital_management.models.Patient;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PatientMapper {

    @Mapping(target = "age", expression = "java(patient.getAge())")
    @Mapping(target = "bmi", expression = "java(patient.getBMI())")
    @Mapping(target = "bmiCategory", expression = "java(patient.getBMICategory())")
    PatientDto toDto(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "patientId", ignore = true) // Will be generated
    @Mapping(target = "isActivePatient", constant = "true")
    @Mapping(target = "registrationDate", expression = "java(java.time.LocalDateTime.now())")
    Patient toEntity(PatientRegistrationDto dto);

    List<PatientDto> toDtoList(List<Patient> patients);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "patientId", ignore = true)
    void updatePatientFromDto(PatientRegistrationDto dto, @MappingTarget Patient patient);
}
