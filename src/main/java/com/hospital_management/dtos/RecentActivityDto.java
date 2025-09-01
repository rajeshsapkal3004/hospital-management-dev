package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentActivityDto {
    private Long id;
    private String action;
    private String description;
    private String username;
    private String userRole;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String resourceType;
    private String resourceId;
    private String severity; // INFO, WARNING, ERROR, CRITICAL
    private String module;
}
