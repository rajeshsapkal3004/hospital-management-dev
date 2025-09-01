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
public class SystemUsageReportDto {
    private String reportPeriod;
    private Long totalRequests;
    private Double averageResponseTime; // in milliseconds
    private Double errorRate; // percentage
    private Integer peakUsageHour;
    private List<EndpointUsageDto> popularEndpoints;
    private Map<String, Long> requestsByModule;
    private Map<String, Double> responseTimesByEndpoint;
    private LocalDateTime generatedAt;
}

