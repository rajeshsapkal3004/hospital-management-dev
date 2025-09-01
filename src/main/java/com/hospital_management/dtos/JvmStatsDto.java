package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JvmStatsDto {
    private String version;
    private Long uptime; // in milliseconds
    private Integer threadCount;
    private Integer peakThreadCount;
    private Long totalStartedThreadCount;
    private Integer daemonThreadCount;
    private GarbageCollectorStatsDto gc;
}