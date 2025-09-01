package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Size(max = 50, message = "Notification type cannot exceed 50 characters")
    @Column(name = "notification_type", length = 50)
    private String notificationType; // APPOINTMENT, PRESCRIPTION, LAB_RESULT, etc.

    @Size(max = 20, message = "Priority cannot exceed 20 characters")
    @Column(name = "priority", length = 20)
    private String priority; // LOW, NORMAL, HIGH, URGENT

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_date")
    private LocalDateTime readDate;

    @Size(max = 500, message = "Action URL cannot exceed 500 characters")
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Size(max = 100, message = "Action text cannot exceed 100 characters")
    @Column(name = "action_text", length = 100)
    private String actionText;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    // Business methods
    public boolean isNew() {
        return createdDate != null && createdDate.isAfter(LocalDateTime.now().minusHours(24));
    }

    public void markAsRead() {
        this.isRead = true;
        this.readDate = LocalDateTime.now();
    }
}
