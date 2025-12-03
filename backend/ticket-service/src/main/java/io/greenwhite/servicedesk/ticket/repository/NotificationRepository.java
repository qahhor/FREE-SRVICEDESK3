package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.common.enums.NotificationCategory;
import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import io.greenwhite.servicedesk.ticket.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a user, ordered by creation date
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find unread notifications for a user
     */
    Page<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndReadAtIsNull(UUID userId);

    /**
     * Find notifications by type for a user
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationChannel type, Pageable pageable);

    /**
     * Find notifications by category for a user
     */
    Page<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(UUID userId, NotificationCategory category, Pageable pageable);

    /**
     * Find notifications by reference
     */
    List<Notification> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);

    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.readAt IS NULL")
    int markAllAsReadForUser(@Param("userId") UUID userId);

    /**
     * Delete old notifications (older than specified days)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < CURRENT_TIMESTAMP - :days * INTERVAL '1 day'")
    int deleteOldNotifications(@Param("days") int days);
}
