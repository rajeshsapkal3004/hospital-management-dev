package com.hospital_management.dtos;

import com.hospital_management.models.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

// BroadcastNotificationDto.java
@Data
public class BroadcastNotificationDto {
    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private String priority; // LOW, NORMAL, HIGH, URGENT

    private List<Role.RoleName> targetRoles; // null means all users

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduleFor; // null means send immediately

    private String type; // INFO, WARNING, ERROR, SUCCESS

    private Boolean requiresAcknowledgment;

    private LocalDateTime expiresAt;
}
