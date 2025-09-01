package com.hospital_management.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SystemHealthDto {
    private String status; // HEALTHY, WARNING, CRITICAL, DOWN
    private DatabaseHealthDto database;
    private MemoryUsageDto memory;
    private DiskUsageDto disk;
    private List<String> warnings;
    private LocalDateTime lastChecked;
    private Double uptime; // in hours
    private String version;
}
