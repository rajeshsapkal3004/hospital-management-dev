package com.hospital_management.dtos;

import com.hospital_management.enumclasses.SystemHealthStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// AdminDashboardDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDto {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalPatients;
    private Long totalDoctors;
    private Long todayAppointments;
    private Long pendingApprovals;
    private SystemHealthStatus systemHealth;
    private List<RecentActivityDto> recentActivities;
    private Map<String, Object> statistics;
}
