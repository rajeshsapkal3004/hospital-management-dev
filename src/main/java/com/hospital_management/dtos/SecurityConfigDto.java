package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecurityConfigDto {
    private Integer maxLoginAttempts;
    private Integer lockoutDuration; // in minutes
    private Boolean enableTwoFactor;
    private Integer jwtExpirationHours;
    private Boolean enablePasswordHistory;
    private Integer passwordHistoryCount;
    private Boolean enableAccountLockout;
    private Boolean enableBruteForceProtection;
}
