package com.hospital_management.dtos;


import com.hospital_management.models.Patient;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class PatientRegistrationDto extends UserRegistrationDto {

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood type must be valid (A+, A-, B+, B-, AB+, AB-, O+, O-)")
    private String bloodType;

    @Size(max = 100, message = "Insurance provider cannot exceed 100 characters")
    private String insuranceProvider;

    @Pattern(regexp = "^[A-Z0-9-]{5,30}$", message = "Insurance number must be 5-30 characters")
    private String insuranceNumber;

    @NotBlank(message = "Emergency contact name is required")
    @Size(min = 2, max = 100, message = "Emergency contact name must be between 2 and 100 characters")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Emergency contact phone must be 10-15 digits")
    private String emergencyContactPhone;

    @Size(max = 50, message = "Emergency contact relationship cannot exceed 50 characters")
    private String emergencyContactRelationship;

    @Size(max = 100, message = "Occupation cannot exceed 100 characters")
    private String occupation;

    private Patient.MaritalStatus maritalStatus;

    @DecimalMin(value = "0.0", message = "Height must be positive")
    @DecimalMax(value = "300.0", message = "Height cannot exceed 300 cm")
    private Double heightCm;

    @DecimalMin(value = "0.0", message = "Weight must be positive")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000 kg")
    private Double weightKg;

    @Size(max = 1000, message = "Medical notes cannot exceed 1000 characters")
    private String medicalNotes;

    private Set<String> allergies;
    private Set<String> medicalConditions;
    private Set<String> currentMedications;
}
