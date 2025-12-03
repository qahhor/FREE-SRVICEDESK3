package io.greenwhite.servicedesk.ticket.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Browser push notification subscription
 */
@Entity
@Table(name = "push_subscriptions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "endpoint"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endpoint;

    @Column(name = "p256dh_key", nullable = false, length = 255)
    private String p256dhKey;

    @Column(name = "auth_key", nullable = false, length = 255)
    private String authKey;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Update last used timestamp
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
