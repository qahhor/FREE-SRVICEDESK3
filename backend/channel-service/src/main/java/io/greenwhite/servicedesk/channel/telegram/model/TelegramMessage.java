package io.greenwhite.servicedesk.channel.telegram.model;

import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity for storing Telegram message history
 */
@Entity
@Table(name = "telegram_messages", indexes = {
    @Index(name = "idx_telegram_messages_chat_id", columnList = "chat_id"),
    @Index(name = "idx_telegram_messages_ticket_id", columnList = "ticket_id"),
    @Index(name = "idx_telegram_messages_created_at", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramMessage extends BaseEntity {

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "direction", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MessageDirection direction;

    @Column(name = "message_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_id", length = 255)
    private String fileId;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Message direction
     */
    public enum MessageDirection {
        INCOMING,  // From user to bot
        OUTGOING   // From bot to user
    }

    /**
     * Message content type
     */
    public enum MessageType {
        TEXT,
        PHOTO,
        DOCUMENT,
        VOICE,
        VIDEO,
        AUDIO,
        STICKER,
        CONTACT,
        LOCATION,
        COMMAND
    }
}
