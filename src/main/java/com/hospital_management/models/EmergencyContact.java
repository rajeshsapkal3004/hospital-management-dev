package com.hospital_management.models;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_contacts")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient"})
@ToString(exclude = {"patient"})
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name cannot exceed 100 characters")
    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @NotBlank(message = "Relationship is required")
    @Size(max = 50, message = "Relationship cannot exceed 50 characters")
    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    @Column(name = "address", length = 200)
    private String address;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}
