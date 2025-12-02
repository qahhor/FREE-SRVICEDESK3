package io.greenwhite.servicedesk.channel.telegram.repository;

import io.greenwhite.servicedesk.channel.telegram.model.TelegramMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for TelegramMessage entity
 */
@Repository
public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, UUID> {

    /**
     * Find messages by chat ID ordered by creation date
     */
    List<TelegramMessage> findByChatIdOrderByCreatedAtDesc(Long chatId);

    /**
     * Find messages by chat ID with pagination
     */
    Page<TelegramMessage> findByChatId(Long chatId, Pageable pageable);

    /**
     * Find messages for a specific ticket
     */
    List<TelegramMessage> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);

    /**
     * Check if message exists by chat ID and message ID
     */
    boolean existsByChatIdAndMessageId(Long chatId, Long messageId);

    /**
     * Find unprocessed incoming messages
     */
    List<TelegramMessage> findByProcessedFalseAndDirection(TelegramMessage.MessageDirection direction);

    /**
     * Count messages for a chat
     */
    long countByChatId(Long chatId);
}
