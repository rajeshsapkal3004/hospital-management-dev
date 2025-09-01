package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserActivityReportDto {
    private String reportPeriod;
    private Long totalLogins;
    private Long uniqueUsers;
    private Double averageSessionDuration; // in minutes
    private List<UserActivitySummaryDto> topActiveUsers;
    private Map<Integer, Long> activityByHour; // hour -> count
    private Map<String, Long> activityByRole;
    private Map<String, Long> activityByAction;
    private LocalDateTime generatedAt;
}
