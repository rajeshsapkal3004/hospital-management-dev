package com.hospital_management.services.auditlogs;

import com.hospital_management.dtos.AuditLogDto;
import com.hospital_management.mapper.AuditLogMapper;
import com.hospital_management.models.AuditLog;
import com.hospital_management.repo.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public CompletableFuture<Void> logUserAction(String action, Long userId, String details) {
        try {
            log.debug("Creating audit log: {} for user: {}", action, userId);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .userId(userId)
                    .username(getCurrentUsername())
                    .details(details)
                    .ipAddress(getCurrentUserIpAddress())
                    .userAgent(getCurrentUserAgent())
                    .timestamp(LocalDateTime.now())
                    .severity(AuditLog.Severity.INFO)
                    .sessionId(getCurrentSessionId())
                    .requestUrl(getCurrentRequestUrl())
                    .httpMethod(getCurrentHttpMethod())
                    .build();

            AuditLog saved = auditLogRepository.save(auditLog);
            log.info("Audit log created successfully: ID={}, Action={}, User={}",
                    saved.getId(), action, userId);

        } catch (Exception e) {
            log.error("Failed to create audit log for action: {}, userId: {}, error: {}",
                    action, userId, e.getMessage(), e);
            // Don't throw exception to prevent breaking the main flow
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> logSecurityEvent(String action, Long userId, String details, AuditLog.Severity severity) {
        try {
            log.debug("Creating security audit log: {} for user: {}", action, userId);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .userId(userId)
                    .username(getCurrentUsername())
                    .details(details)
                    .ipAddress(getCurrentUserIpAddress())
                    .userAgent(getCurrentUserAgent())
                    .timestamp(LocalDateTime.now())
                    .severity(severity)
                    .sessionId(getCurrentSessionId())
                    .requestUrl(getCurrentRequestUrl())
                    .httpMethod(getCurrentHttpMethod())
                    .build();

            AuditLog saved = auditLogRepository.save(auditLog);

            if (severity == AuditLog.Severity.ERROR || severity == AuditLog.Severity.CRITICAL) {
                log.warn("High severity security event logged: ID={}, Action={}, User={}",
                        saved.getId(), action, userId);
            } else {
                log.info("Security audit log created: ID={}, Action={}, User={}",
                        saved.getId(), action, userId);
            }

        } catch (Exception e) {
            log.error("Failed to create security audit log for action: {}, userId: {}, error: {}",
                    action, userId, e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> logResourceAccess(String action, Long userId, String resource, Long resourceId, String details) {
        try {
            log.debug("Creating resource access audit log: {} for user: {}, resource: {}", action, userId, resource);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .userId(userId)
                    .username(getCurrentUsername())
                    .details(details)
                    .resource(resource)
                    .resourceId(resourceId)
                    .ipAddress(getCurrentUserIpAddress())
                    .userAgent(getCurrentUserAgent())
                    .timestamp(LocalDateTime.now())
                    .severity(AuditLog.Severity.INFO)
                    .sessionId(getCurrentSessionId())
                    .requestUrl(getCurrentRequestUrl())
                    .httpMethod(getCurrentHttpMethod())
                    .build();

            AuditLog saved = auditLogRepository.save(auditLog);
            log.info("Resource access audit log created: ID={}, Action={}, Resource={}({})",
                    saved.getId(), action, resource, resourceId);

        } catch (Exception e) {
            log.error("Failed to create resource access audit log for action: {}, resource: {}, error: {}",
                    action, resource, e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> logSystemEvent(String action, String details, AuditLog.Severity severity) {
        try {
            log.debug("Creating system audit log: {}", action);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .username("SYSTEM")
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .severity(severity)
                    .build();

            AuditLog saved = auditLogRepository.save(auditLog);
            log.info("System audit log created: ID={}, Action={}", saved.getId(), action);

        } catch (Exception e) {
            log.error("Failed to create system audit log for action: {}, error: {}",
                    action, e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogs(Pageable pageable) {
        log.debug("Fetching audit logs with pagination: {}", pageable);
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsByUser(Long userId, Pageable pageable) {
        log.debug("Fetching audit logs for user: {}", userId);
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsByAction(String action, Pageable pageable) {
        log.debug("Fetching audit logs for action: {}", action);
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Fetching audit logs for date range: {} to {}", startDate, endDate);
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> searchAuditLogs(Long userId, String username, String action, String resource,
                                             AuditLog.Severity severity, String ipAddress,
                                             LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Searching audit logs with filters");
        return auditLogRepository.findAuditLogsWithFilters(userId, username, action, resource,
                        severity, ipAddress, startDate, endDate, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentFailedLogins(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        log.debug("Fetching failed logins in last {} hours", hours);
        return auditLogMapper.toDtoList(auditLogRepository.findRecentFailedLogins(since));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getHighSeverityEvents(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        log.debug("Fetching high severity events in last {} hours", hours);
        return auditLogMapper.toDtoList(auditLogRepository.findRecentHighSeverityLogs(since));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getSecurityEvents(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching security events between {} and {}", startDate, endDate);
        List<AuditLog> securityLogs = auditLogRepository.findSecurityRelatedLogs();
        List<AuditLog> filteredLogs = securityLogs.stream()
                .filter(auditLog -> auditLog.getTimestamp().isAfter(startDate) && auditLog.getTimestamp().isBefore(endDate))
                .toList();
        return auditLogMapper.toDtoList(filteredLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getActionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching action statistics between {} and {}", startDate, endDate);
        List<Object[]> results = auditLogRepository.getActionStatistics(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] row : results) {
            String action = (String) row[0];
            Number countNum = (Number) row[1];
            long count = (countNum == null) ? 0L : countNum.longValue();
            statistics.put(action, count);
        }
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getUserActivityStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching user activity statistics between {} and {}", startDate, endDate);
        List<Object[]> results = auditLogRepository.getUserActivityStatistics(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] row : results) {
            String username = (String) row[0];
            Number countNum = (Number) row[1];
            long count = (countNum == null) ? 0L : countNum.longValue();
            statistics.put(username, count);
        }
        return statistics;
    }
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getSeverityStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching severity statistics between {} and {}", startDate, endDate);
        List<Object[]> results = auditLogRepository.getSeverityStatistics(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] row : results) {
            AuditLog.Severity severity = (AuditLog.Severity) row[0];
            Number countNum = (Number) row[1];
            long count = (countNum == null) ? 0L : countNum.longValue();
            statistics.put(severity.name(), count);
        }
        return statistics;
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<Object[]> getDailyActivityStatistics(LocalDateTime startDate, LocalDateTime endDate) {
//        log.debug("Fetching daily activity statistics between {} and {}", startDate, endDate);
//        return auditLogRepository.getDailyActivityStatistics(startDate, endDate);
//    }

    @Override
    @Transactional
    public void cleanupOldAuditLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        log.info("Cleaning up audit logs older than {} days", retentionDays);

        long countToDelete = auditLogRepository.countByTimestampBefore(cutoffDate);
        auditLogRepository.deleteByTimestampBefore(cutoffDate);

        log.info("Deleted {} old audit log entries", countToDelete);
        logSystemEvent("AUDIT_LOGS_CLEANUP",
                String.format("Cleaned up %d audit logs older than %d days", countToDelete, retentionDays),
                AuditLog.Severity.INFO);
    }

    @Override
    @Transactional
    public void cleanupAuditLogsBySeverity(AuditLog.Severity severity, int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        log.info("Cleaning up {} severity audit logs older than {} days", severity, retentionDays);

        long countToDelete = auditLogRepository.countBySeverityAndTimestampBefore(severity, cutoffDate);
        auditLogRepository.deleteBySeverityAndTimestampBefore(severity, cutoffDate);

        log.info("Cleaned up {} {} severity audit logs older than {} days", countToDelete, severity, retentionDays);
        logSystemEvent("AUDIT_LOGS_CLEANUP_BY_SEVERITY",
                String.format("Cleaned up %d %s severity audit logs older than %d days", countToDelete, severity, retentionDays),
                AuditLog.Severity.INFO);
    }

    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Could not get current username: {}", e.getMessage());
        }
        return "anonymous";
    }

    private String getCurrentUserIpAddress() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        } catch (Exception e) {
            log.debug("Could not get IP address: {}", e.getMessage());
            return "unknown";
        }
    }

    private String getCurrentUserAgent() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null ? userAgent : "unknown";
        } catch (Exception e) {
            log.debug("Could not get user agent: {}", e.getMessage());
            return "unknown";
        }
    }

    private String getCurrentSessionId() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            return request.getSession().getId();
        } catch (Exception e) {
            log.debug("Could not get session ID: {}", e.getMessage());
            return "unknown";
        }
    }

    private String getCurrentRequestUrl() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            return request.getRequestURL().toString();
        } catch (Exception e) {
            log.debug("Could not get request URL: {}", e.getMessage());
            return "unknown";
        }
    }

    private String getCurrentHttpMethod() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            return request.getMethod();
        } catch (Exception e) {
            log.debug("Could not get HTTP method: {}", e.getMessage());
            return "unknown";
        }
    }
}
