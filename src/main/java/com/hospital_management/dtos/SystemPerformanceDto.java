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
public class SystemPerformanceDto {
    private Double cpuUsage; // percentage
    private Double memoryUsage; // percentage
    private Double diskUsage; // percentage
    private Integer activeConnections;
    private Double requestsPerSecond;
    private Double averageResponseTime; // in milliseconds
    private Double throughput; // requests per minute
    private Map<String, Double> endpointResponseTimes;
    private LocalDateTime timestamp;
    private NetworkStatsDto network;
    private JvmStatsDto jvm;
}
