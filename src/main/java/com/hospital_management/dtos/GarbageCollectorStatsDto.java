package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GarbageCollectorStatsDto {
    private Long totalCollections;
    private Long totalCollectionTime; // in milliseconds
    private Double averageCollectionTime;
    private Map<String, Long> collectorStats;
}