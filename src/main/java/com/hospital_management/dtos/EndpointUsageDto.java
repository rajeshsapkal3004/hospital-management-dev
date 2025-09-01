package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointUsageDto {
    private String endpoint;
    private String method;
    private Long requestCount;
    private Double averageResponseTime;
    private Double errorRate;
    private Long totalBytes;
}