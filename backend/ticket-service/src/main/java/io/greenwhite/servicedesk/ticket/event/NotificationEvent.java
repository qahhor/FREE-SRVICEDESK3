package io.greenwhite.servicedesk.ticket.event;

import io.greenwhite.servicedesk.ticket.dto.NotificationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event for notification updates sent via WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private String type;
    private NotificationResponse notification;
    private String message;
    private LocalDateTime timestamp;

    public static NotificationEvent created(NotificationResponse notification) {
        return NotificationEvent.builder()
                .type("NOTIFICATION_CREATED")
                .notification(notification)
                .message("New notification received")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static NotificationEvent read(NotificationResponse notification) {
        return NotificationEvent.builder()
                .type("NOTIFICATION_READ")
                .notification(notification)
                .message("Notification marked as read")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static NotificationEvent countUpdate(long count) {
        return NotificationEvent.builder()
                .type("NOTIFICATION_COUNT_UPDATE")
                .notification(null)
                .message(String.valueOf(count))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
