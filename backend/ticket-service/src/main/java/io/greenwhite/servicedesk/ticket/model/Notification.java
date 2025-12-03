package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.enums.NotificationCategory;
import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Notification entity for storing in-app and other notifications
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationChannel type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationCategory category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    /**
     * Check if notification is read
     */
    public boolean isRead() {
        return readAt != null;
    }
}
