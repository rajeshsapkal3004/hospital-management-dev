package com.hospital_management.dtos;



import com.hospital_management.models.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private LocalDateTime appointmentDateTime;
    private String appointmentType;
    private String reason;
    private String notes;
    private Appointment.AppointmentStatus status;
    private Integer duration;
    private String rescheduleReason;
    private String cancellationReason;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private Boolean isFollowUp;
    private Long followUpFor;
    private String roomNumber;
    private Appointment.AppointmentPriority priority;
    private String symptoms;
    private Boolean isEmergency;
    private String referenceNumber;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    // Computed fields
    private String statusDisplayName;
    private String priorityDisplayName;
    private Boolean canBeRescheduled;
    private Boolean canBeCancelled;
    private Boolean isUpcoming;
    private Boolean isPast;
    private String timeUntilAppointment;
    private String appointmentDurationFormatted;

    // Helper methods for display
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }

    public String getPriorityDisplayName() {
        return priority != null ? priority.getDisplayName() : null;
    }

    public Boolean getCanBeRescheduled() {
        return status == Appointment.AppointmentStatus.SCHEDULED ||
                status == Appointment.AppointmentStatus.CONFIRMED;
    }

    public Boolean getCanBeCancelled() {
        return status != Appointment.AppointmentStatus.COMPLETED &&
                status != Appointment.AppointmentStatus.CANCELLED;
    }

    public Boolean getIsUpcoming() {
        return appointmentDateTime != null &&
                appointmentDateTime.isAfter(LocalDateTime.now()) &&
                (status == Appointment.AppointmentStatus.SCHEDULED ||
                        status == Appointment.AppointmentStatus.CONFIRMED);
    }

    public Boolean getIsPast() {
        return appointmentDateTime != null &&
                appointmentDateTime.isBefore(LocalDateTime.now());
    }

    public String getAppointmentDurationFormatted() {
        if (duration == null) return null;
        if (duration < 60) {
            return duration + " minutes";
        } else {
            int hours = duration / 60;
            int minutes = duration % 60;
            if (minutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + " hour" + (hours > 1 ? "s" : "") + " " +
                        minutes + " minute" + (minutes > 1 ? "s" : "");
            }
        }
    }
}
