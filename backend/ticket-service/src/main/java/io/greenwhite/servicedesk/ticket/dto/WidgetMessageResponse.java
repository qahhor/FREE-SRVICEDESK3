package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.ticket.model.WidgetMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for widget message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetMessageResponse {

    private UUID id;
    private UUID sessionId;
    private String senderType;
    private UUID senderId;
    private String senderName;
    private String content;
    private String messageType;
    private UUID attachmentId;
    private String attachmentUrl;
    private String attachmentName;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    /**
     * Create response from entity
     */
    public static WidgetMessageResponse fromEntity(WidgetMessage message) {
        return WidgetMessageResponse.builder()
                .id(message.getId())
                .sessionId(message.getSession().getId())
                .senderType(message.getSenderType().name())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .messageType(message.getMessageType().name())
                .attachmentId(message.getAttachment() != null ? message.getAttachment().getId() : null)
                .attachmentUrl(message.getAttachment() != null ? 
                        "/api/v1/attachments/" + message.getAttachment().getId() + "/download" : null)
                .attachmentName(message.getAttachment() != null ? 
                        message.getAttachment().getOriginalFilename() : null)
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Create response from entity with sender name
     */
    public static WidgetMessageResponse fromEntity(WidgetMessage message, String senderName) {
        WidgetMessageResponse response = fromEntity(message);
        response.setSenderName(senderName);
        return response;
    }
}
