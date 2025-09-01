package com.hospital_management.repo;


import com.hospital_management.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Basic queries
    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByUsername(String username);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByResource(String resource);

    List<AuditLog> findBySeverity(AuditLog.Severity severity);

    List<AuditLog> findByResourceAndResourceId(String resource, Long resourceId);

    List<AuditLog> findBySessionId(String sessionId);

    List<AuditLog> findByIpAddress(String ipAddress);

    // Time-based queries
    List<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByTimestampAfter(LocalDateTime date);

    List<AuditLog> findByTimestampBefore(LocalDateTime date);

    @Query("SELECT al FROM AuditLog al WHERE al.timestamp >= :date ORDER BY al.timestamp DESC")
    List<AuditLog> findRecentLogs(@Param("date") LocalDateTime date);

    @Query("SELECT al FROM AuditLog al WHERE DATE(al.timestamp) = CURRENT_DATE ORDER BY al.timestamp DESC")
    List<AuditLog> findTodaysLogs();

    @Query("SELECT al FROM AuditLog al WHERE al.timestamp >= :startOfWeek AND al.timestamp < :endOfWeek ORDER BY al.timestamp DESC")
    List<AuditLog> findThisWeeksLogs(@Param("startOfWeek") LocalDateTime startOfWeek, @Param("endOfWeek") LocalDateTime endOfWeek);

    // User-specific queries
    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    List<AuditLog> findByUserIdAndTimestampBetween(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al FROM AuditLog al WHERE al.username = :username AND al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    List<AuditLog> findByUsernameAndTimestampBetween(@Param("username") String username,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.action = :action ORDER BY al.timestamp DESC")
    List<AuditLog> findByUserIdAndAction(@Param("userId") Long userId, @Param("action") String action);

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.resource = :resource ORDER BY al.timestamp DESC")
    List<AuditLog> findByUserIdAndResource(@Param("userId") Long userId, @Param("resource") String resource);

    // Action-specific queries
    @Query("SELECT al FROM AuditLog al WHERE al.action = :action AND al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    Page<AuditLog> findByActionAndTimestampBetween(@Param("action") String action,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.action LIKE :actionPattern ORDER BY al.timestamp DESC")
    List<AuditLog> findByActionPattern(@Param("actionPattern") String actionPattern);

    @Query("SELECT al FROM AuditLog al WHERE al.action IN :actions ORDER BY al.timestamp DESC")
    List<AuditLog> findByActionIn(@Param("actions") List<String> actions);

    // Resource-specific queries
    @Query("SELECT al FROM AuditLog al WHERE al.resource = :resource AND al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    List<AuditLog> findByResourceAndTimestampBetween(@Param("resource") String resource,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al FROM AuditLog al WHERE al.resource = :resource AND al.resourceId = :resourceId ORDER BY al.timestamp DESC")
    List<AuditLog> findByResourceAndResourceIdOrderByTimestampDesc(@Param("resource") String resource, @Param("resourceId") Long resourceId);

    // Severity-based queries
    @Query("SELECT al FROM AuditLog al WHERE al.severity IN :severities ORDER BY al.timestamp DESC")
    List<AuditLog> findBySeverityIn(@Param("severities") List<AuditLog.Severity> severities);

    @Query("SELECT al FROM AuditLog al WHERE al.severity = :severity AND al.timestamp >= :date ORDER BY al.timestamp DESC")
    List<AuditLog> findBySeverityAndTimestampAfter(@Param("severity") AuditLog.Severity severity, @Param("date") LocalDateTime date);

    @Query("SELECT al FROM AuditLog al WHERE al.severity IN ('ERROR', 'CRITICAL') ORDER BY al.timestamp DESC")
    List<AuditLog> findHighSeverityLogs();

    @Query("SELECT al FROM AuditLog al WHERE al.severity IN ('ERROR', 'CRITICAL') AND al.timestamp >= :date ORDER BY al.timestamp DESC")
    List<AuditLog> findRecentHighSeverityLogs(@Param("date") LocalDateTime date);

    // Security-related queries
    @Query("SELECT al FROM AuditLog al WHERE al.action LIKE '%LOGIN%' OR al.action LIKE '%LOGOUT%' OR al.action LIKE '%ACCESS_DENIED%' ORDER BY al.timestamp DESC")
    List<AuditLog> findSecurityRelatedLogs();

    @Query("SELECT al FROM AuditLog al WHERE al.action = 'LOGIN_FAILED' AND al.timestamp >= :date ORDER BY al.timestamp DESC")
    List<AuditLog> findRecentFailedLogins(@Param("date") LocalDateTime date);

    @Query("SELECT al FROM AuditLog al WHERE al.action = 'LOGIN_FAILED' AND al.userId = :userId ORDER BY al.timestamp DESC")
    List<AuditLog> findFailedLoginsByUser(@Param("userId") Long userId);

    @Query("SELECT al FROM AuditLog al WHERE al.action = 'LOGIN_FAILED' AND al.ipAddress = :ipAddress ORDER BY al.timestamp DESC")
    List<AuditLog> findFailedLoginsByIpAddress(@Param("ipAddress") String ipAddress);

    // HTTP method queries (if applicable)
    List<AuditLog> findByHttpMethod(String httpMethod);

    @Query("SELECT al FROM AuditLog al WHERE al.httpMethod = :method AND al.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByHttpMethodAndTimestampBetween(@Param("method") String method,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    // Search and filter queries
    @Query("SELECT al FROM AuditLog al WHERE " +
            "(:userId IS NULL OR al.userId = :userId) AND " +
            "(:username IS NULL OR al.username = :username) AND " +
            "(:action IS NULL OR al.action = :action) AND " +
            "(:resource IS NULL OR al.resource = :resource) AND " +
            "(:severity IS NULL OR al.severity = :severity) AND " +
            "(:ipAddress IS NULL OR al.ipAddress = :ipAddress) AND " +
            "(:startDate IS NULL OR al.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR al.timestamp <= :endDate) " +
            "ORDER BY al.timestamp DESC")
    Page<AuditLog> findAuditLogsWithFilters(@Param("userId") Long userId,
                                            @Param("username") String username,
                                            @Param("action") String action,
                                            @Param("resource") String resource,
                                            @Param("severity") AuditLog.Severity severity,
                                            @Param("ipAddress") String ipAddress,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE " +
            "LOWER(al.details) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(al.action) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(al.resource) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AuditLog> findByKeyword(@Param("keyword") String keyword);

    // Count queries
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.userId = :userId AND al.action = :action")
    Long countByUserIdAndAction(@Param("userId") Long userId, @Param("action") String action);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.action = :action AND al.timestamp BETWEEN :startDate AND :endDate")
    Long countByActionAndTimestampBetween(@Param("action") String action,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.severity = :severity AND al.timestamp >= :date")
    Long countBySeverityAndTimestampAfter(@Param("severity") AuditLog.Severity severity, @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.resource = :resource AND al.timestamp BETWEEN :startDate AND :endDate")
    Long countByResourceAndTimestampBetween(@Param("resource") String resource,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.ipAddress = :ipAddress AND al.timestamp >= :date")
    Long countByIpAddressAndTimestampAfter(@Param("ipAddress") String ipAddress, @Param("date") LocalDateTime date);

    // Statistical queries
    @Query("SELECT al.action, COUNT(al) FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate GROUP BY al.action ORDER BY COUNT(al) DESC")
    List<Object[]> getActionStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al.resource, COUNT(al) FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate GROUP BY al.resource ORDER BY COUNT(al) DESC")
    List<Object[]> getResourceStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al.severity, COUNT(al) FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate GROUP BY al.severity ORDER BY COUNT(al) DESC")
    List<Object[]> getSeverityStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al.username, COUNT(al) FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate AND al.username IS NOT NULL GROUP BY al.username ORDER BY COUNT(al) DESC")
    List<Object[]> getUserActivityStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al.ipAddress, COUNT(al) FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate AND al.ipAddress IS NOT NULL GROUP BY al.ipAddress ORDER BY COUNT(al) DESC")
    List<Object[]> getIpAddressStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DATE(al.timestamp), COUNT(al) FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate GROUP BY DATE(al.timestamp) ORDER BY DATE(al.timestamp)")
    List<Object[]> getDailyActivityStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT HOUR(al.timestamp), COUNT(al) FROM AuditLog al WHERE DATE(al.timestamp) = CURRENT_DATE GROUP BY HOUR(al.timestamp) ORDER BY HOUR(al.timestamp)")
    List<Object[]> getHourlyActivityStatistics();

    // Cleanup queries
    @Modifying
    @Query("DELETE FROM AuditLog al WHERE al.timestamp < :date")
    void deleteByTimestampBefore(@Param("date") LocalDateTime date);

    @Modifying
    @Query("DELETE FROM AuditLog al WHERE al.severity = :severity AND al.timestamp < :date")
    void deleteBySeverityAndTimestampBefore(@Param("severity") AuditLog.Severity severity, @Param("date") LocalDateTime date);

    @Modifying
    @Query("DELETE FROM AuditLog al WHERE al.action = :action AND al.timestamp < :date")
    void deleteByActionAndTimestampBefore(@Param("action") String action, @Param("date") LocalDateTime date);

    // Distinct value queries
    @Query("SELECT DISTINCT al.action FROM AuditLog al ORDER BY al.action")
    List<String> findDistinctActions();

    @Query("SELECT DISTINCT al.resource FROM AuditLog al WHERE al.resource IS NOT NULL ORDER BY al.resource")
    List<String> findDistinctResources();

    @Query("SELECT DISTINCT al.ipAddress FROM AuditLog al WHERE al.ipAddress IS NOT NULL ORDER BY al.ipAddress")
    List<String> findDistinctIpAddresses();

    @Query("SELECT DISTINCT al.username FROM AuditLog al WHERE al.username IS NOT NULL ORDER BY al.username")
    List<String> findDistinctUsernames();

    // Performance queries
    @Query(value = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<AuditLog> findLatestLogs(@Param("limit") int limit);

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId ORDER BY al.timestamp DESC")
    Page<AuditLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.action = :action ORDER BY al.timestamp DESC")
    Page<AuditLog> findByActionOrderByTimestampDesc(@Param("action") String action, Pageable pageable);

    // Add these methods to AuditLogRepository interface

    // For count operations before delete
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.timestamp < :date")
    Long countByTimestampBefore(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.severity = :severity AND al.timestamp < :date")
    Long countBySeverityAndTimestampBefore(@Param("severity") AuditLog.Severity severity, @Param("date") LocalDateTime date);

    // Updated method to return Page instead of List
    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(@Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate,
                                                              Pageable pageable);

    List<AuditLog> findTop10ByOrderByTimestampDesc();

}
