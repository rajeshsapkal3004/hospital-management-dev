package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

// NotificationConfigDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationConfigDto {
    private Boolean enableInAppNotifications;
    private Boolean enableEmailNotifications;
    private Boolean enableSmsNotifications;
    private Boolean enablePushNotifications;
    private Integer retentionDays;
    private Map<String, Boolean> notificationTypes;
}
