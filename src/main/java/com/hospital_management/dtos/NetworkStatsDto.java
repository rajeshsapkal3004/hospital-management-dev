package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NetworkStatsDto {
    private Long bytesReceived;
    private Long bytesSent;
    private Long packetsReceived;
    private Long packetsSent;
    private Double networkUtilization; // percentage
}
