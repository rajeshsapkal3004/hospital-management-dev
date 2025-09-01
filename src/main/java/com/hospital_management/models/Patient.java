package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "patients", uniqueConstraints = {
        @UniqueConstraint(columnNames = "patient_id"),
        @UniqueConstraint(columnNames = "insurance_number")
})
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("PATIENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = {"appointments", "medicalRecords"})
@ToString(callSuper = true, exclude = {"appointments", "medicalRecords"})
public class Patient extends User{

    @NotBlank(message = "Patient ID is required")
    @Size(min = 5, max = 20, message = "Patient ID must be between 5 and 20 characters")
    @Pattern(regexp = "^PAT[0-9]{6,15}$", message = "Patient ID must start with 'PAT' followed by 6-15 digits")
    @Column(name = "patient_id", unique = true, nullable = false, length = 20)
    private String patientId;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood type must be valid (A+, A-, B+, B-, AB+, AB-, O+, O-)")
    @Column(name = "blood_type", length = 3)
    private String bloodType;

    @Size(max = 100, message = "Insurance provider cannot exceed 100 characters")
    @Column(name = "insurance_provider", length = 100)
    private String insuranceProvider;

    @Pattern(regexp = "^[A-Z0-9-]{5,30}$", message = "Insurance number must be 5-30 characters, uppercase letters, numbers, and hyphens only")
    @Column(name = "insurance_number", unique = true, length = 30)
    private String insuranceNumber;

    @NotBlank(message = "Emergency contact name is required")
    @Size(min = 2, max = 100, message = "Emergency contact name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Emergency contact name can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "emergency_contact_name", nullable = false, length = 100)
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Emergency contact phone must be 10-15 digits")
    @Column(name = "emergency_contact_phone", nullable = false, length = 15)
    private String emergencyContactPhone;

    @Size(max = 50, message = "Emergency contact relationship cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Relationship can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "emergency_contact_relationship", length = 50)
    private String emergencyContactRelationship;

    @Size(max = 100, message = "Occupation cannot exceed 100 characters")
    @Column(name = "occupation", length = 100)
    private String occupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @DecimalMin(value = "0.0", message = "Height must be positive")
    @DecimalMax(value = "300.0", message = "Height cannot exceed 300 cm")
    @Column(name = "height_cm")
    private Double heightCm;

    @DecimalMin(value = "0.0", message = "Weight must be positive")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000 kg")
    @Column(name = "weight_kg")
    private Double weightKg;

    @Builder.Default
    @Column(name = "is_active_patient", nullable = false)
    private Boolean isActivePatient = true;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "last_visit_date")
    private LocalDateTime lastVisitDate;

    @Size(max = 1000, message = "Medical notes cannot exceed 1000 characters")
    @Column(name = "medical_notes", length = 1000)
    private String medicalNotes;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "patient_allergies",
            joinColumns = @JoinColumn(name = "patient_id")
    )
    @Column(name = "allergy", length = 200)
    @Size(max = 200, message = "Each allergy description cannot exceed 200 characters")
    @Builder.Default
    private Set<String> allergies = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "patient_medical_conditions",
            joinColumns = @JoinColumn(name = "patient_id")
    )
    @Column(name = "medical_condition")  // Renamed column
    private Set<String> medicalConditions = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "patient_medications",
            joinColumns = @JoinColumn(name = "patient_id")
    )
    @Column(name = "medication", length = 200)
    @Size(max = 200, message = "Each medication cannot exceed 200 characters")
    @Builder.Default
    private Set<String> currentMedications = new HashSet<>();

    @Size(max = 200, message = "Employer cannot exceed 200 characters")
    @Column(name = "employer", length = 200)
    private String employer;

    @Size(max = 2000, message = "Medical history cannot exceed 2000 characters")
    @Column(name = "medical_history", length = 2000)
    private String medicalHistory;

    @Size(max = 1000, message = "Family history cannot exceed 1000 characters")
    @Column(name = "family_history", length = 1000)
    private String familyHistory;

    @Size(max = 500, message = "Social history cannot exceed 500 characters")
    @Column(name = "social_history", length = 500)
    private String socialHistory;

    @Size(max = 100, message = "Preferred language cannot exceed 100 characters")
    @Column(name = "preferred_language", length = 100)
    private String preferredLanguage;

    @Size(max = 100, message = "Religion cannot exceed 100 characters")
    @Column(name = "religion", length = 100)
    private String religion;

    @Column(name = "smoking_status")
    private Boolean smokingStatus;

    @Column(name = "drinking_status")
    private Boolean drinkingStatus;

    @Size(max = 500, message = "Profile photo URL cannot exceed 500 characters")
    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    // Relationship mappings (to be defined when other entities are created)
    // @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<Appointment> appointments = new HashSet<>();

    // @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<MedicalRecord> medicalRecords = new HashSet<>();

    // Custom constructors
    public Patient(String username, String email, String password, String firstName, String lastName,
                   LocalDate dateOfBirth, String emergencyContactName, String emergencyContactPhone) {
        super(username, email, password, firstName, lastName);
        this.dateOfBirth = dateOfBirth;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.isActivePatient = true;
        this.registrationDate = LocalDateTime.now();
        this.allergies = new HashSet<>();
        this.medicalConditions = new HashSet<>();
        this.currentMedications = new HashSet<>();
    }

    // Business methods
    public int getAge() {
        return LocalDate.now().getYear() - this.dateOfBirth.getYear();
    }

    public double getBMI() {
        if (heightCm != null && weightKg != null && heightCm > 0) {
            double heightInMeters = heightCm / 100.0;
            return weightKg / (heightInMeters * heightInMeters);
        }
        return 0.0;
    }

    public String getBMICategory() {
        double bmi = getBMI();
        if (bmi < 18.5) return "Underweight";
        else if (bmi < 25) return "Normal weight";
        else if (bmi < 30) return "Overweight";
        else return "Obese";
    }

    public void addAllergy(String allergy) {
        if (allergy != null && !allergy.trim().isEmpty()) {
            this.allergies.add(allergy.trim());
        }
    }

    public void removeAllergy(String allergy) {
        this.allergies.remove(allergy);
    }

    public void addMedicalCondition(String condition) {
        if (condition != null && !condition.trim().isEmpty()) {
            this.medicalConditions.add(condition.trim());
        }
    }

    public void removeMedicalCondition(String condition) {
        this.medicalConditions.remove(condition);
    }

    public void addCurrentMedication(String medication) {
        if (medication != null && !medication.trim().isEmpty()) {
            this.currentMedications.add(medication.trim());
        }
    }

    public void removeCurrentMedication(String medication) {
        this.currentMedications.remove(medication);
    }

    public boolean hasAllergy(String allergy) {
        return this.allergies.contains(allergy);
    }

    public boolean hasMedicalCondition(String condition) {
        return this.medicalConditions.contains(condition);
    }

    public void updateLastVisit() {
        this.lastVisitDate = LocalDateTime.now();
    }

    public enum MaritalStatus {
        SINGLE("Single"),
        MARRIED("Married"),
        DIVORCED("Divorced"),
        WIDOWED("Widowed"),
        SEPARATED("Separated"),
        DOMESTIC_PARTNER("Domestic Partner");

        private final String displayName;

        MaritalStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

}
