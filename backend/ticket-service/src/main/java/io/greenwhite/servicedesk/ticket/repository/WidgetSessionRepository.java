package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.WidgetSession;
import io.greenwhite.servicedesk.ticket.model.WidgetSession.WidgetSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for widget sessions
 */
@Repository
public interface WidgetSessionRepository extends JpaRepository<WidgetSession, UUID> {

    /**
     * Find session by session token
     */
    Optional<WidgetSession> findBySessionToken(String sessionToken);

    /**
     * Find session by ID and status
     */
    Optional<WidgetSession> findByIdAndStatus(UUID id, WidgetSessionStatus status);

    /**
     * Check if session token exists
     */
    boolean existsBySessionToken(String sessionToken);

    /**
     * Find session by ticket ID
     */
    Optional<WidgetSession> findByTicketId(UUID ticketId);
}
