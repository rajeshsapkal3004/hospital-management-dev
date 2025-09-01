package com.hospital_management.models;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "allergies")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient"})
@ToString(exclude = {"patient"})
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @NotBlank(message = "Allergen is required")
    @Size(max = 200, message = "Allergen cannot exceed 200 characters")
    @Column(name = "allergen", nullable = false, length = 200)
    private String allergen;

    @Size(max = 50, message = "Allergy type cannot exceed 50 characters")
    @Column(name = "allergy_type", length = 50)
    private String allergyType; // DRUG, FOOD, ENVIRONMENTAL, etc.

    @Size(max = 20, message = "Severity cannot exceed 20 characters")
    @Column(name = "severity", length = 20)
    private String severity; // MILD, MODERATE, SEVERE

    @Size(max = 500, message = "Reaction cannot exceed 500 characters")
    @Column(name = "reaction", length = 500)
    private String reaction;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "onset_date")
    private LocalDateTime onsetDate;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Size(max = 100, message = "Reported by cannot exceed 100 characters")
    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @Column(name = "reported_date")
    private LocalDateTime reportedDate;

    @Size(max = 100, message = "Verified by cannot exceed 100 characters")
    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Business methods
    public boolean isSevere() {
        return "SEVERE".equalsIgnoreCase(severity);
    }

    public boolean requiresAttention() {
        return isSevere();
    }
}
