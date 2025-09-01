package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient", "doctor"})
@ToString(exclude = {"patient", "doctor"})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment must be scheduled for a future date and time")
    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;

    @NotBlank(message = "Appointment type is required")
    @Size(max = 50, message = "Appointment type cannot exceed 50 characters")
    @Column(name = "appointment_type", nullable = false, length = 50)
    private String appointmentType;

    @NotBlank(message = "Reason for appointment is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 240, message = "Duration cannot exceed 240 minutes")
    @Builder.Default
    @Column(name = "duration", nullable = false)
    private Integer duration = 30; // Duration in minutes

    @Size(max = 200, message = "Reschedule reason cannot exceed 200 characters")
    @Column(name = "reschedule_reason", length = 200)
    private String rescheduleReason;

    @Size(max = 200, message = "Cancellation reason cannot exceed 200 characters")
    @Column(name = "cancellation_reason", length = 200)
    private String cancellationReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Builder.Default
    @Column(name = "is_follow_up", nullable = false)
    private Boolean isFollowUp = false;

    @Column(name = "follow_up_for")
    private Long followUpFor; // Reference to previous appointment ID

    @Size(max = 100, message = "Room number cannot exceed 100 characters")
    @Column(name = "room_number", length = 100)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priority", nullable = false, length = 20)
    private AppointmentPriority priority = AppointmentPriority.NORMAL;

    @Size(max = 100, message = "Symptoms cannot exceed 100 characters")
    @Column(name = "symptoms", length = 100)
    private String symptoms;

    @Builder.Default
    @Column(name = "is_emergency", nullable = false)
    private Boolean isEmergency = false;

    @Size(max = 20, message = "Reference number cannot exceed 20 characters")
    @Column(name = "reference_number", unique = true, length = 20)
    private String referenceNumber;

    // Audit fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Appointment Status Enum
    public enum AppointmentStatus {
        SCHEDULED("Scheduled"),
        CONFIRMED("Confirmed"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        NO_SHOW("No Show"),
        RESCHEDULED("Rescheduled");

        private final String displayName;

        AppointmentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Appointment Priority Enum
    public enum AppointmentPriority {
        LOW("Low"),
        NORMAL("Normal"),
        HIGH("High"),
        URGENT("Urgent"),
        EMERGENCY("Emergency");

        private final String displayName;

        AppointmentPriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business methods
    public boolean canBeRescheduled() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }

    public boolean canBeCancelled() {
        return status != AppointmentStatus.COMPLETED && status != AppointmentStatus.CANCELLED;
    }

    public boolean isUpcoming() {
        return appointmentDateTime.isAfter(LocalDateTime.now()) &&
                (status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED);
    }

    public boolean isPast() {
        return appointmentDateTime.isBefore(LocalDateTime.now());
    }

    public void markAsCompleted() {
        this.status = AppointmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsCancelled(String reason) {
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void reschedule(LocalDateTime newDateTime, String reason) {
        this.appointmentDateTime = newDateTime;
        this.rescheduleReason = reason;
        this.status = AppointmentStatus.RESCHEDULED;
    }

    @PrePersist
    protected void onCreate() {
        if (referenceNumber == null) {
            referenceNumber = generateReferenceNumber();
        }
    }

    private String generateReferenceNumber() {
        return "APT-" + System.currentTimeMillis();
    }
}
