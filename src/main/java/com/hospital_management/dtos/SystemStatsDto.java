package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

// SystemStatsDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemStatsDto {
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long lockedUsers;
    private Map<String, Long> usersByRole;
    private Long totalDepartments;
    private Long totalRoles;
    private Long totalPermissions;
    private String systemUptime;
    private LocalDateTime lastUpdated;
}