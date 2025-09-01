package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient"})
@ToString(exclude = {"patient"})
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    @Column(name = "category", nullable = false, length = 50)
    private String category; // SERVICE, FACILITY, STAFF, etc.

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Column(name = "rating")
    private Integer rating;

    @Size(max = 20, message = "Status cannot exceed 20 characters")
    @Column(name = "status", length = 20)
    private String status; // SUBMITTED, REVIEWED, RESOLVED

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Size(max = 2000, message = "Response cannot exceed 2000 characters")
    @Column(name = "response", length = 2000)
    private String response;

    @Column(name = "response_date")
    private LocalDateTime responseDate;

    @Size(max = 100, message = "Responded by cannot exceed 100 characters")
    @Column(name = "responded_by", length = 100)
    private String respondedBy;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}
