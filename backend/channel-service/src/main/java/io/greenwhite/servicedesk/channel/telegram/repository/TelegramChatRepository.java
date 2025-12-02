package io.greenwhite.servicedesk.channel.telegram.repository;

import io.greenwhite.servicedesk.channel.telegram.model.TelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TelegramChat entity
 */
@Repository
public interface TelegramChatRepository extends JpaRepository<TelegramChat, UUID> {

    /**
     * Find chat by Telegram chat ID
     */
    Optional<TelegramChat> findByChatId(Long chatId);

    /**
     * Check if chat exists by Telegram chat ID
     */
    boolean existsByChatId(Long chatId);

    /**
     * Find chat by username
     */
    Optional<TelegramChat> findByUsername(String username);

    /**
     * Find chat linked to a user account
     */
    Optional<TelegramChat> findByUserId(UUID userId);
}
