package com.hospital_management.models;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "vital_signs")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient"})
@ToString(exclude = {"patient"})
public class VitalSigns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @NotNull(message = "Recorded date is required")
    @Column(name = "recorded_date", nullable = false)
    private LocalDateTime recordedDate;

    @DecimalMin(value = "35.0", message = "Temperature must be at least 35°C")
    @DecimalMax(value = "45.0", message = "Temperature cannot exceed 45°C")
    @Column(name = "temperature")
    private Double temperature; // in Celsius

    @Min(value = 70, message = "Systolic pressure must be at least 70")
    @Max(value = 250, message = "Systolic pressure cannot exceed 250")
    @Column(name = "systolic_pressure")
    private Integer systolicPressure;

    @Min(value = 40, message = "Diastolic pressure must be at least 40")
    @Max(value = 150, message = "Diastolic pressure cannot exceed 150")
    @Column(name = "diastolic_pressure")
    private Integer diastolicPressure;

    @Min(value = 40, message = "Heart rate must be at least 40")
    @Max(value = 200, message = "Heart rate cannot exceed 200")
    @Column(name = "heart_rate")
    private Integer heartRate; // beats per minute

    @Min(value = 10, message = "Respiratory rate must be at least 10")
    @Max(value = 40, message = "Respiratory rate cannot exceed 40")
    @Column(name = "respiratory_rate")
    private Integer respiratoryRate; // breaths per minute

    @Min(value = 70, message = "Oxygen saturation must be at least 70%")
    @Max(value = 100, message = "Oxygen saturation cannot exceed 100%")
    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation; // percentage

    @DecimalMin(value = "1.0", message = "Weight must be at least 1 kg")
    @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg")
    @Column(name = "weight")
    private Double weight; // in kg

    @DecimalMin(value = "30.0", message = "Height must be at least 30 cm")
    @DecimalMax(value = "250.0", message = "Height cannot exceed 250 cm")
    @Column(name = "height")
    private Double height; // in cm

    @Column(name = "bmi")
    private Double bmi;

    @Size(max = 10, message = "Pain level cannot exceed 10 characters")
    @Column(name = "pain_level", length = 10)
    private String painLevel; // 0-10 scale

    @Size(max = 50, message = "Consciousness cannot exceed 50 characters")
    @Column(name = "consciousness", length = 50)
    private String consciousness; // Alert, Drowsy, etc.

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    @Size(max = 100, message = "Recorded by cannot exceed 100 characters")
    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @Size(max = 20, message = "Recorded by type cannot exceed 20 characters")
    @Column(name = "recorded_by_type", length = 20)
    private String recordedByType; // DOCTOR, NURSE, etc.

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Business methods
    @PrePersist
    @PreUpdate
    protected void calculateBMI() {
        if (weight != null && height != null && height > 0) {
            double heightInMeters = height / 100.0;
            this.bmi = weight / (heightInMeters * heightInMeters);
        }
    }

    public String getBloodPressure() {
        if (systolicPressure != null && diastolicPressure != null) {
            return systolicPressure + "/" + diastolicPressure;
        }
        return null;
    }
}
