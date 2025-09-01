package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DatabaseHealthDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseHealthDto {
    private String status; // UP, DOWN, SLOW
    private Long connectionPoolSize;
    private Long activeConnections;
    private Long idleConnections;
    private Double responseTime; // in milliseconds
    private String databaseVersion;
    private Long totalSize; // in MB
    private Double diskUsage; // percentage
}