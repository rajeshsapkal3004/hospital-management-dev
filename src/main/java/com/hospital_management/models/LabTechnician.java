package com.hospital_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lab_technicians", uniqueConstraints = {
        @UniqueConstraint(columnNames = "employee_id"),
        @UniqueConstraint(columnNames = "certification_number")
})
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("LAB_TECHNICIAN")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LabTechnician extends User{

    @NotBlank(message = "Employee ID is required for lab technicians")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Employee ID can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;

    @Size(min = 5, max = 50, message = "Certification number must be between 5 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Certification number can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "certification_number", unique = true, length = 50)
    private String certificationNumber;

    @NotBlank(message = "Department is required for lab technicians")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", length = 20)
    private Shift shift;

    @Future(message = "Certification expiry date must be in the future")
    @Column(name = "certification_expiry")
    private LocalDate certificationExpiry;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Size(max = 100, message = "Lab section cannot exceed 100 characters")
    @Column(name = "lab_section", length = 100)
    private String labSection;

    @Size(max = 100, message = "Supervisor name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Supervisor name can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "supervisor_name", length = 100)
    private String supervisorName;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 40, message = "Years of experience cannot exceed 40")
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Builder.Default
    @Column(name = "can_approve_results", nullable = false)
    private Boolean canApproveResults = false;

    @Builder.Default
    @Column(name = "can_operate_equipment", nullable = false)
    private Boolean canOperateEquipment = true;

    @Builder.Default
    @Column(name = "is_senior_technician", nullable = false)
    private Boolean isSeniorTechnician = false;

    @Size(max = 500, message = "Qualifications cannot exceed 500 characters")
    @Column(name = "qualifications", length = 500)
    private String qualifications;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "lab_technician_specializations",
            joinColumns = @JoinColumn(name = "technician_id")
    )
    @Column(name = "specialization", length = 100)
    @Size(max = 100, message = "Each specialization cannot exceed 100 characters")
    @Builder.Default
    private Set<String> specializations = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "lab_technician_equipment",
            joinColumns = @JoinColumn(name = "technician_id")
    )
    @Column(name = "equipment", length = 100)
    @Size(max = 100, message = "Each equipment name cannot exceed 100 characters")
    @Builder.Default
    private Set<String> certifiedEquipment = new HashSet<>();

    // Custom constructors
    public LabTechnician(String username, String email, String password, String firstName, String lastName,
                         String employeeId, String department) {
        super(username, email, password, firstName, lastName);
        this.employeeId = employeeId;
        this.department = department;
        this.canApproveResults = false;
        this.canOperateEquipment = true;
        this.isSeniorTechnician = false;
        this.specializations = new HashSet<>();
        this.certifiedEquipment = new HashSet<>();
    }

    // Business methods
    public boolean isCertificationValid() {
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

    public void addCertifiedEquipment(String equipment) {
        if (equipment != null && !equipment.trim().isEmpty()) {
            this.certifiedEquipment.add(equipment.trim());
        }
    }

    public void removeCertifiedEquipment(String equipment) {
        this.certifiedEquipment.remove(equipment);
    }

    public String getTechnicianCode() {
        return "LAB-" + this.employeeId;
    }

    public boolean canOperateEquipment(String equipment) {
        return this.canOperateEquipment &&
                (this.certifiedEquipment.isEmpty() || this.certifiedEquipment.contains(equipment));
    }

    public boolean canProcessTest(String testType) {
        return this.isEnabled() && this.isAccountNonLocked() &&
                isCertificationValid() && this.canOperateEquipment;
    }

    public enum Shift {
        MORNING("Morning Shift", "06:00-14:00"),
        AFTERNOON("Afternoon Shift", "14:00-22:00"),
        NIGHT("Night Shift", "22:00-06:00"),
        DAY("Day Shift", "08:00-16:00"),
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

    /**
     * Remove equipment certification from the technician
     * @param equipment The equipment name to remove certification for
     */
    public void removeEquipmentCertification(String equipment) {
        if (this.certifiedEquipment != null && equipment != null) {
            this.certifiedEquipment.remove(equipment.trim());
        }
    }

    /**
     * Add equipment certification to the technician
     * @param equipment The equipment name to add certification for
     */
    public void addEquipmentCertification(String equipment) {
        if (equipment != null && !equipment.trim().isEmpty()) {
            if (this.certifiedEquipment == null) {
                this.certifiedEquipment = new HashSet<>();
            }
            this.certifiedEquipment.add(equipment.trim());
        }
    }
}
