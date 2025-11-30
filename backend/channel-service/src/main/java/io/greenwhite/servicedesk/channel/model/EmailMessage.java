package io.greenwhite.servicedesk.channel.model;

import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Email message entity for tracking incoming/outgoing emails
 */
@Entity
@Table(name = "email_messages", indexes = {
    @Index(name = "idx_email_ticket", columnList = "ticket_id"),
    @Index(name = "idx_email_message_id", columnList = "message_id"),
    @Index(name = "idx_email_received_at", columnList = "received_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessage extends BaseEntity {

    @Column(name = "message_id", unique = true, length = 255)
    private String messageId; // Email Message-ID header

    @Column(name = "ticket_id")
    private String ticketId; // Linked ticket UUID

    @Column(name = "from_address", nullable = false, length = 255)
    private String fromAddress;

    @Column(name = "from_name", length = 255)
    private String fromName;

    @Column(name = "to_addresses", nullable = false, length = 1000)
    private String toAddresses; // Comma-separated

    @Column(name = "cc_addresses", length = 1000)
    private String ccAddresses; // Comma-separated

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText; // Plain text body

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml; // HTML body

    @Column(name = "direction", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EmailDirection direction; // INBOUND, OUTBOUND

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EmailStatus status; // RECEIVED, PROCESSED, FAILED, SENT

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "has_attachments", nullable = false)
    private Boolean hasAttachments = false;

    @Column(name = "attachment_count")
    private Integer attachmentCount = 0;

    @Column(name = "in_reply_to", length = 255)
    private String inReplyTo; // In-Reply-To header for threading

    @Column(name = "references", length = 1000)
    private String references; // References header for threading

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // Error details if processing failed

    public enum EmailDirection {
        INBOUND,
        OUTBOUND
    }

    public enum EmailStatus {
        RECEIVED,    // Email received, not yet processed
        PROCESSED,   // Successfully processed and ticket created/updated
        FAILED,      // Processing failed
        SENT,        // Outbound email sent successfully
        SENDING      // Outbound email in progress
    }
}
