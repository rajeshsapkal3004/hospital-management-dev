package com.hospital_management.dtos;



import com.hospital_management.models.Appointment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentDto {

    private Long patientId; // Set automatically from authentication

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment must be scheduled for a future date and time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime appointmentDateTime;

    @NotBlank(message = "Appointment type is required")
    @Size(max = 50, message = "Appointment type cannot exceed 50 characters")
    private String appointmentType;

    @NotBlank(message = "Reason for appointment is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 240, message = "Duration cannot exceed 240 minutes")
    private Integer duration = 30;

    @Builder.Default
    private Appointment.AppointmentPriority priority = Appointment.AppointmentPriority.NORMAL;

    @Size(max = 100, message = "Symptoms cannot exceed 100 characters")
    private String symptoms;

    @Builder.Default
    private Boolean isEmergency = false;

    @Builder.Default
    private Boolean isFollowUp = false;

    private Long followUpFor; // Previous appointment ID if this is a follow-up

    // Validation method
    public boolean isValidTimeSlot() {
        if (appointmentDateTime == null) return false;

        // Check if it's within business hours (8 AM to 6 PM)
        int hour = appointmentDateTime.getHour();
        return hour >= 8 && hour <= 18;
    }
}
