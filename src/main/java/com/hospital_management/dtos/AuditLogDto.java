package com.hospital_management.dtos;


import com.hospital_management.models.AuditLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDto {
    private Long id;
    private String action;
    private Long userId;
    private String username;
    private String details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private AuditLog.Severity severity;
    private String resource;
    private Long resourceId;
    private String sessionId;
    private String requestUrl;
    private String httpMethod;
    private String oldValues;
    private String newValues;
    private boolean isHighPriority;
    private boolean isSecurityRelated;
}
