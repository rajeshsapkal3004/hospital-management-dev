package com.hospital_management.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class RescheduleAppointmentDto {

    @NotNull(message = "New appointment date and time is required")
    @Future(message = "New appointment must be scheduled for a future date and time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime newDateTime;

    @Size(max = 200, message = "Reason cannot exceed 200 characters")
    private String reason;
}

