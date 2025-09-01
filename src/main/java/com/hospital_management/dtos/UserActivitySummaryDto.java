package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserActivitySummaryDto {
    private Long userId;
    private String username;
    private String fullName;
    private String role;
    private Long loginCount;
    private Long actionCount;
    private LocalDateTime lastActivity;
    private Double averageSessionDuration;
}