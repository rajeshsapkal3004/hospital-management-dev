package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// MemoryUsageDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemoryUsageDto {
    private Long used; // in MB
    private Long total; // in MB
    private Long free; // in MB
    private Double usagePercentage;
    private Long heapUsed; // in MB
    private Long heapMax; // in MB
    private Long nonHeapUsed; // in MB
    private Long nonHeapMax; // in MB
}