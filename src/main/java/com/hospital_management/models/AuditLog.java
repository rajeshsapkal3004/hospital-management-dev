package com.hospital_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_resource", columnList = "resource")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Action is required")
    @Size(min = 2, max = 100, message = "Action must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Action must contain only uppercase letters and underscores")
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "user_id")
    private Long userId;

    @Size(max = 50, message = "Username cannot exceed 50 characters")
    @Column(name = "username", length = 50)
    private String username;

    @Size(max = 1000, message = "Details cannot exceed 1000 characters")
    @Column(name = "details", length = 1000)
    private String details;

    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", message = "Invalid IP address format")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 500, message = "User agent cannot exceed 500 characters")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20, nullable = false)
    @Builder.Default
    private Severity severity = Severity.INFO;

    @Size(max = 100, message = "Resource cannot exceed 100 characters")
    @Pattern(regexp = "^[A-Z_]*$", message = "Resource must contain only uppercase letters and underscores")
    @Column(name = "resource", length = 100)
    private String resource;

    @Column(name = "resource_id")
    private Long resourceId;

    @Size(max = 100, message = "Session ID cannot exceed 100 characters")
    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Size(max = 200, message = "Request URL cannot exceed 200 characters")
    @Column(name = "request_url", length = 200)
    private String requestUrl;

    @Size(max = 10, message = "HTTP method cannot exceed 10 characters")
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)$", message = "Invalid HTTP method")
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Size(max = 1000, message = "Old values cannot exceed 1000 characters")
    @Column(name = "old_values", length = 1000)
    private String oldValues;

    @Size(max = 1000, message = "New values cannot exceed 1000 characters")
    @Column(name = "new_values", length = 1000)
    private String newValues;

    // Custom constructors
    public AuditLog(String action, Long userId, String details) {
        this.action = action;
        this.userId = userId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
        this.severity = Severity.INFO;
    }

    public AuditLog(String action, Long userId, String details, Severity severity) {
        this(action, userId, details);
        this.severity = severity;
    }

    public AuditLog(String action, Long userId, String username, String details, String resource, Long resourceId) {
        this.action = action;
        this.userId = userId;
        this.username = username;
        this.details = details;
        this.resource = resource;
        this.resourceId = resourceId;
        this.timestamp = LocalDateTime.now();
        this.severity = Severity.INFO;
    }

    // Business methods
    public boolean isHighPriority() {
        return this.severity == Severity.ERROR || this.severity == Severity.CRITICAL;
    }

    public boolean isSecurityRelated() {
        return this.action != null && (
                this.action.contains("LOGIN") ||
                        this.action.contains("LOGOUT") ||
                        this.action.contains("PERMISSION") ||
                        this.action.contains("ROLE") ||
                        this.action.contains("ACCESS_DENIED")
        );
    }

    public String getFormattedTimestamp() {
        return this.timestamp.toString();
    }

    @Getter
    public enum Severity {
        INFO("Information"),
        WARNING("Warning"),
        ERROR("Error"),
        CRITICAL("Critical");

        private final String displayName;

        Severity(String displayName) {
            this.displayName = displayName;
        }
    }

}
