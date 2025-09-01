package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DiskUsageDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiskUsageDto {
    private Long used; // in GB
    private Long total; // in GB
    private Long free; // in GB
    private Double usagePercentage;
    private String path;
    private List<DiskPartitionDto> partitions;
}

