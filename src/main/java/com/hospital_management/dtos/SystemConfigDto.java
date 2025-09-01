package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfigDto {
    private Boolean maintenanceMode;
    private String maintenanceMessage;
    private Integer maxLoginAttempts;
    private Integer sessionTimeout; // in minutes
    private Integer passwordExpiryDays;
    private Map<String, Object> passwordPolicy;
    private SecurityConfigDto security;
    private EmailConfigDto email;
    private NotificationConfigDto notifications;
    private FileStorageConfigDto fileStorage;
    private LocalDateTime lastUpdated;
    private String updatedBy;
}