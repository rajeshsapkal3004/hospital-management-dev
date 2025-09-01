package com.hospital_management.dtos;

// AppointmentSlotDto.java

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlotDto {

    private LocalDateTime dateTime;
    private Boolean available;
    private Integer duration;
    private String reason; // If not available, reason why
}