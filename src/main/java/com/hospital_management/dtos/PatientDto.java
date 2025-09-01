package com.hospital_management.dtos;


import com.hospital_management.models.Patient;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PatientDto extends UserDto {
    private String patientId;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String insuranceProvider;
    private String insuranceNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    private String occupation;
    private Patient.MaritalStatus maritalStatus;
    private Double heightCm;
    private Double weightKg;
    private boolean isActivePatient;
    private LocalDateTime registrationDate;
    private LocalDateTime lastVisitDate;
    private String medicalNotes;
    private Set<String> allergies;
    private Set<String> medicalConditions;
    private Set<String> currentMedications;
    private Integer age;
    private Double bmi;
    private String bmiCategory;
}
