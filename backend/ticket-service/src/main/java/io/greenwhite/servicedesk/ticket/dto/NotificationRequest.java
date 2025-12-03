package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.NotificationCategory;
import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import io.greenwhite.servicedesk.common.enums.NotificationEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a notification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Event type is required")
    private NotificationEventType eventType;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Category is required")
    private NotificationCategory category;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private Map<String, Object> data;

    private String referenceType;

    private UUID referenceId;
}
