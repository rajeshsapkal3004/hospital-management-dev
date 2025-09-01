package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String notificationType; // APPOINTMENT, PRESCRIPTION, LAB_RESULT, etc.
    private String priority; // LOW, NORMAL, HIGH, URGENT
    private Boolean isRead;
    private LocalDateTime readDate;
    private LocalDateTime createdDate;
    private String actionUrl;
    private String actionText;

    // Computed fields
    private String priorityDisplayName;
    private String timeAgo;
    private Boolean isNew; // created within last 24 hours

    public Boolean getIsNew() {
        return createdDate != null && createdDate.isAfter(LocalDateTime.now().minusHours(24));
    }
}