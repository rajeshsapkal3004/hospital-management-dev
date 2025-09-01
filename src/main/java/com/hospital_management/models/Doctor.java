package com.hospital_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "doctors", uniqueConstraints = {
        @UniqueConstraint(columnNames = "license_number"),
        @UniqueConstraint(columnNames = "npi_number")
})
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("DOCTOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = {"appointments", "medicalRecords"})
@ToString(callSuper = true, exclude = {"appointments", "medicalRecords"})
public class Doctor extends User {

    @NotBlank(message = "License number is required for doctors")
    @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "License number can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "license_number", unique = true, nullable = false, length = 50)
    private String licenseNumber;

    @NotBlank(message = "Specialization is required for doctors")
    @Size(min = 2, max = 100, message = "Specialization must be between 2 and 100 characters")
    @Column(name = "specialization", nullable = false, length = 100)
    private String specialization;

    @NotBlank(message = "Department is required for doctors")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Future(message = "License expiry date must be in the future")
    @Column(name = "license_expiry")
    private LocalDate licenseExpiry;

    @Pattern(regexp = "^[0-9]{10}$", message = "NPI number must be exactly 10 digits")
    @Column(name = "npi_number", unique = true, length = 10)
    private String npiNumber;

    @Size(max = 20, message = "Employee ID cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Employee ID can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "employee_id", length = 20)
    private String employeeId;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be positive")
    @DecimalMax(value = "9999.99", message = "Consultation fee cannot exceed 9999.99")
    @Column(name = "consultation_fee")
    private BigDecimal consultationFee;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Years of experience cannot exceed 60")
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Size(max = 500, message = "Qualifications cannot exceed 500 characters")
    @Column(name = "qualifications", length = 500)
    private String qualifications;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    @Column(name = "bio", length = 1000)
    private String bio;

    @Builder.Default
    @Column(name = "is_available_for_consultation", nullable = false)
    private Boolean isAvailableForConsultation = true;

    @Builder.Default
    @Column(name = "is_emergency_contact", nullable = false)
    private Boolean isEmergencyContact = false;

    @Pattern(regexp = "^[A-Za-z0-9]{1,20}$", message = "Room number must be alphanumeric and max 20 characters")
    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "doctor_certifications",
            joinColumns = @JoinColumn(name = "doctor_id")
    )
    @Column(name = "certification", length = 200)
    @Builder.Default
    private Set<String> certifications = new HashSet<>();

    // Relationship mappings (to be defined when other entities are created)
    // @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<Appointment> appointments = new HashSet<>();

    // @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<MedicalRecord> medicalRecords = new HashSet<>();

    // Custom constructors
    public Doctor(String username, String email, String password, String firstName, String lastName,
                  String licenseNumber, String specialization, String department) {
        super(username, email, password, firstName, lastName);
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        this.department = department;
        this.isAvailableForConsultation = true;
        this.isEmergencyContact = false;
        this.certifications = new HashSet<>();
    }

    // Business methods
    public boolean isLicenseValid() {
        return licenseExpiry == null || licenseExpiry.isAfter(LocalDate.now());
    }

    public void addCertification(String certification) {
        if (certification != null && !certification.trim().isEmpty()) {
            this.certifications.add(certification.trim());
        }
    }

    public void removeCertification(String certification) {
        this.certifications.remove(certification);
    }

    public String getDoctorCode() {
        return "DR-" + (this.employeeId != null ? this.employeeId : this.getId());
    }

    public boolean canConsult() {
        return this.isAvailableForConsultation && this.isEnabled() &&
                this.isAccountNonLocked() && isLicenseValid();
    }

}
