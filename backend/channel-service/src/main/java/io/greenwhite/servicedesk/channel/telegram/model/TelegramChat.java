package io.greenwhite.servicedesk.channel.telegram.model;

import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity for storing Telegram chat mappings
 */
@Entity
@Table(name = "telegram_chats", indexes = {
    @Index(name = "idx_telegram_chats_chat_id", columnList = "chat_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramChat extends BaseEntity {

    @Column(name = "chat_id", unique = true, nullable = false)
    private Long chatId;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Column(name = "current_state", length = 50)
    @Enumerated(EnumType.STRING)
    private ConversationState currentState = ConversationState.IDLE;

    @Column(name = "pending_ticket_subject", length = 500)
    private String pendingTicketSubject;

    @Column(name = "pending_ticket_priority", length = 20)
    private String pendingTicketPriority;

    /**
     * Conversation state for multi-step operations
     */
    public enum ConversationState {
        IDLE,
        AWAITING_TICKET_SUBJECT,
        AWAITING_TICKET_DESCRIPTION,
        AWAITING_TICKET_PRIORITY,
        AWAITING_TICKET_CONFIRMATION
    }

    /**
     * Get full name combining first and last name
     */
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null) {
            name.append(firstName);
        }
        if (lastName != null) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(lastName);
        }
        return name.length() > 0 ? name.toString() : username;
    }
}
