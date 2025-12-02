package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import io.greenwhite.servicedesk.common.enums.NotificationEventType;
import io.greenwhite.servicedesk.ticket.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NotificationTemplate entity
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    /**
     * Find template by name
     */
    Optional<NotificationTemplate> findByName(String name);

    /**
     * Find templates by event type and channel
     */
    List<NotificationTemplate> findByEventTypeAndChannelAndIsActiveTrue(NotificationEventType eventType, NotificationChannel channel);

    /**
     * Find all active templates for an event type
     */
    List<NotificationTemplate> findByEventTypeAndIsActiveTrue(NotificationEventType eventType);

    /**
     * Find all templates by channel
     */
    List<NotificationTemplate> findByChannelAndIsActiveTrue(NotificationChannel channel);

    /**
     * Find template by event type, channel, and language
     */
    Optional<NotificationTemplate> findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
            NotificationEventType eventType, 
            NotificationChannel channel, 
            String language
    );
}
