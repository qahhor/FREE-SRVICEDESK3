package io.greenwhite.servicedesk.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

/**
 * User notification preferences
 */
@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled")
    private Boolean inAppEnabled = true;

    @Column(name = "push_enabled")
    private Boolean pushEnabled = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "email_settings", columnDefinition = "jsonb")
    private Map<String, Boolean> emailSettings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "in_app_settings", columnDefinition = "jsonb")
    private Map<String, Boolean> inAppSettings;

    @Column(name = "quiet_hours_enabled")
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(length = 100)
    private String timezone = "UTC";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if a specific email notification type is enabled
     */
    public boolean isEmailNotificationEnabled(String eventType) {
        if (emailEnabled == null || !emailEnabled) return false;
        if (emailSettings == null) return true;
        return emailSettings.getOrDefault(eventType.toLowerCase(), true);
    }

    /**
     * Check if a specific in-app notification type is enabled
     */
    public boolean isInAppNotificationEnabled(String eventType) {
        if (inAppEnabled == null || !inAppEnabled) return false;
        if (inAppSettings == null) return true;
        return inAppSettings.getOrDefault(eventType.toLowerCase(), true);
    }

    /**
     * Check if currently in quiet hours
     */
    public boolean isInQuietHours() {
        if (quietHoursEnabled == null || !quietHoursEnabled || quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Handles overnight quiet hours (e.g., 22:00 to 06:00)
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }
}
