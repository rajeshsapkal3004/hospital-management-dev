package com.hospital_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "nurses", uniqueConstraints = {
        @UniqueConstraint(columnNames = "license_number"),
        @UniqueConstraint(columnNames = "employee_id")
})
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("NURSE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Nurse extends User{

    @NotBlank(message = "License number is required for nurses")
    @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "License number can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "license_number", unique = true, nullable = false, length = 50)
    private String licenseNumber;

    @NotBlank(message = "Employee ID is required for nurses")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Employee ID can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;


    @NotBlank(message = "Department is required for nurses")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", length = 20)
    private Shift shift;

    @Future(message = "Certification expiry date must be in the future")
    @Column(name = "certification_expiry")
    private LocalDate certificationExpiry;

    @NotBlank(message = "Nurse type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "nurse_type", nullable = false, length = 30)
    private NurseType nurseType;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Size(max = 50, message = "Ward assignment cannot exceed 50 characters")
    @Column(name = "ward_assignment", length = 50)
    private String wardAssignment;

    @Size(max = 100, message = "Supervisor name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Supervisor name can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "supervisor_name", length = 100)
    private String supervisorName;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Builder.Default
    @Column(name = "is_head_nurse", nullable = false)
    private Boolean isHeadNurse = false;

    @Builder.Default
    @Column(name = "is_available_for_duty", nullable = false)
    private Boolean isAvailableForDuty = true;

    @Builder.Default
    @Column(name = "can_administer_medication", nullable = false)
    private Boolean canAdministerMedication = true;

    @Size(max = 500, message = "Qualifications cannot exceed 500 characters")
    @Column(name = "qualifications", length = 500)
    private String qualifications;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "nurse_specializations",
            joinColumns = @JoinColumn(name = "nurse_id")
    )
    @Column(name = "specialization", length = 100)
    @Size(max = 100, message = "Each specialization cannot exceed 100 characters")
    @Builder.Default
    private Set<String> specializations = new HashSet<>();


    // Custom constructors
    public Nurse(String username, String email, String password, String firstName, String lastName,
                 String licenseNumber, String employeeId, String department, NurseType nurseType) {
        super(username, email, password, firstName, lastName);
        this.licenseNumber = licenseNumber;
        this.employeeId = employeeId;
        this.department = department;
        this.nurseType = nurseType;
        this.isHeadNurse = false;
        this.isAvailableForDuty = true;
        this.canAdministerMedication = true;
        this.specializations = new HashSet<>();
    }

    // Business methods
    public boolean isLicenseValid() {
        return certificationExpiry == null || certificationExpiry.isAfter(LocalDate.now());
    }

    public boolean canWorkShift(Shift requestedShift) {
        return this.shift == null || this.shift == requestedShift;
    }

    public void addSpecialization(String specialization) {
        if (specialization != null && !specialization.trim().isEmpty()) {
            this.specializations.add(specialization.trim());
        }
    }

    public void removeSpecialization(String specialization) {
        this.specializations.remove(specialization);
    }

    public String getNurseCode() {
        return "RN-" + this.employeeId;
    }

    public boolean canPerformDuty() {
        return this.isAvailableForDuty && this.isEnabled() &&
                this.isAccountNonLocked() && isLicenseValid();
    }

    public enum Shift {
        DAY("Day Shift", "07:00-19:00"),
        NIGHT("Night Shift", "19:00-07:00"),
        MORNING("Morning Shift", "06:00-14:00"),
        EVENING("Evening Shift", "14:00-22:00"),
        ROTATING("Rotating Shift", "Variable");

        private final String displayName;
        private final String timeRange;

        Shift(String displayName, String timeRange) {
            this.displayName = displayName;
            this.timeRange = timeRange;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getTimeRange() {
            return timeRange;
        }
    }

    public enum NurseType {
        REGISTERED_NURSE("Registered Nurse"),
        LICENSED_PRACTICAL_NURSE("Licensed Practical Nurse"),
        CERTIFIED_NURSING_ASSISTANT("Certified Nursing Assistant"),
        NURSE_PRACTITIONER("Nurse Practitioner"),
        CLINICAL_NURSE_SPECIALIST("Clinical Nurse Specialist"),
        CERTIFIED_REGISTERED_NURSE_ANESTHETIST("Certified Registered Nurse Anesthetist"),
        CERTIFIED_NURSE_MIDWIFE("Certified Nurse Midwife");

        private final String displayName;

        NurseType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

}
