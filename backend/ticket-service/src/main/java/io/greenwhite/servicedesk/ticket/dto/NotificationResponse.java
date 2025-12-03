package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.NotificationCategory;
import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for notification data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private NotificationChannel type;
    private NotificationCategory category;
    private String title;
    private String message;
    private Map<String, Object> data;
    private String referenceType;
    private UUID referenceId;
    private LocalDateTime readAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private boolean read;
}
