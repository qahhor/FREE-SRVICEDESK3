package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.WidgetMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for widget messages
 */
@Repository
public interface WidgetMessageRepository extends JpaRepository<WidgetMessage, UUID> {

    /**
     * Find all messages for a session ordered by creation time
     */
    List<WidgetMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    /**
     * Count unread messages for a session
     */
    @Query("SELECT COUNT(m) FROM WidgetMessage m WHERE m.session.id = :sessionId AND m.readAt IS NULL AND m.senderType = 'AGENT'")
    long countUnreadBySessionId(UUID sessionId);

    /**
     * Mark all messages from agents as read
     */
    @Modifying
    @Query("UPDATE WidgetMessage m SET m.readAt = CURRENT_TIMESTAMP WHERE m.session.id = :sessionId AND m.readAt IS NULL AND m.senderType = 'AGENT'")
    int markAllAsReadBySessionId(UUID sessionId);
}
