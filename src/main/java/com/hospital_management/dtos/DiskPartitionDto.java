package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiskPartitionDto {
    private String name;
    private String mountPoint;
    private Long size; // in GB
    private Long used; // in GB
    private Long available; // in GB
    private Double usagePercentage;
}