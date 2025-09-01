package com.hospital_management.repo;

import com.hospital_management.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    List<Notification> findByUserIdOrderByCreatedDateDesc(Long userId);
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "ORDER BY n.createdDate DESC LIMIT 10")
    List<Notification> findRecentNotificationsForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Integer countUnreadForUser(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = :isRead " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findByUserIdAndReadStatus(@Param("userId") Long userId,
                                                 @Param("isRead") Boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.priority = 'URGENT' " +
            "AND n.isRead = false AND n.createdDate >= :since")
    List<Notification> findUrgentUnreadNotifications(@Param("since") LocalDateTime since);
}
