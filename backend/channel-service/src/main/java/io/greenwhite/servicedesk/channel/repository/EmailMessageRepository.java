package io.greenwhite.servicedesk.channel.repository;

import io.greenwhite.servicedesk.channel.model.EmailMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailMessage entity
 */
@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {

    /**
     * Check if email with this Message-ID already exists
     */
    boolean existsByMessageId(String messageId);

    /**
     * Find email by Message-ID
     */
    Optional<EmailMessage> findByMessageId(String messageId);

    /**
     * Find all emails for a ticket
     */
    List<EmailMessage> findByTicketIdOrderByReceivedAtDesc(String ticketId);

    /**
     * Find emails by In-Reply-To header (for threading)
     */
    List<EmailMessage> findByInReplyTo(String inReplyTo);

    /**
     * Find emails from a specific address
     */
    List<EmailMessage> findByFromAddressOrderByReceivedAtDesc(String fromAddress);
}
