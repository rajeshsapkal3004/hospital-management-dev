package com.hospital_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient"})
@ToString(exclude = {"patient"})
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @NotBlank(message = "Insurance provider is required")
    @Size(max = 100, message = "Insurance provider cannot exceed 100 characters")
    @Column(name = "insurance_provider", nullable = false, length = 100)
    private String insuranceProvider;

    @NotBlank(message = "Policy number is required")
    @Size(max = 50, message = "Policy number cannot exceed 50 characters")
    @Column(name = "policy_number", nullable = false, length = 50)
    private String policyNumber;

    @Size(max = 50, message = "Group number cannot exceed 50 characters")
    @Column(name = "group_number", length = 50)
    private String groupNumber;

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Size(max = 100, message = "Subscriber name cannot exceed 100 characters")
    @Column(name = "subscriber_name", length = 100)
    private String subscriberName;

    @Size(max = 100, message = "Subscriber ID cannot exceed 100 characters")
    @Column(name = "subscriber_id", length = 100)
    private String subscriberId;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = true;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Size(max = 20, message = "Copay amount cannot exceed 20 characters")
    @Column(name = "copay_amount", length = 20)
    private String copayAmount;

    @Size(max = 20, message = "Deductible amount cannot exceed 20 characters")
    @Column(name = "deductible_amount", length = 20)
    private String deductibleAmount;

    @Size(max = 1000, message = "Coverage details cannot exceed 1000 characters")
    @Column(name = "coverage_details", length = 1000)
    private String coverageDetails;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Business methods
    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return isActive && !isExpired();
    }
}
