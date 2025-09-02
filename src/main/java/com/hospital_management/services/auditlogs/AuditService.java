package com.hospital_management.services.auditlogs;

import com.hospital_management.dtos.AuditLogDto;
import com.hospital_management.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AuditService {
    // Async audit logging
    CompletableFuture<Void> logUserAction(String action, Long userId, String details);

    CompletableFuture<Void> logSecurityEvent(String action, Long userId, String details, AuditLog.Severity severity);

    CompletableFuture<Void> logResourceAccess(String action, Long userId, String resource, Long resourceId, String details);

    CompletableFuture<Void> logSystemEvent(String action, String details, AuditLog.Severity severity);

    // Query audit logs
    Page<AuditLogDto> getAuditLogs(Pageable pageable);

    Page<AuditLogDto> getAuditLogsByUser(Long userId, Pageable pageable);

    Page<AuditLogDto> getAuditLogsByAction(String action, Pageable pageable);

    Page<AuditLogDto> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<AuditLogDto> searchAuditLogs(Long userId, String username, String action, String resource,
                                      AuditLog.Severity severity, String ipAddress,
                                      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Security monitoring
    List<AuditLogDto> getRecentFailedLogins(int hours);

    List<AuditLogDto> getHighSeverityEvents(int hours);

    List<AuditLogDto> getSecurityEvents(LocalDateTime startDate, LocalDateTime endDate);

    // Statistics
    Map<String, Long> getActionStatistics(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Long> getUserActivityStatistics(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Long> getSeverityStatistics(LocalDateTime startDate, LocalDateTime endDate);

   // List<Object[]> getDailyActivityStatistics(LocalDateTime startDate, LocalDateTime endDate);

    // Cleanup
    void cleanupOldAuditLogs(int retentionDays);

    void cleanupAuditLogsBySeverity(AuditLog.Severity severity, int retentionDays);
}
