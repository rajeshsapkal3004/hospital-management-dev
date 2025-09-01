package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailConfigDto {
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private Boolean enableTls;
    private Boolean enableSsl;
    private String fromEmail;
    private String fromName;
    private Boolean enableEmailNotifications;
}