package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.ticket.dto.NotificationResponse;
import io.greenwhite.servicedesk.ticket.event.NotificationEvent;
import io.greenwhite.servicedesk.ticket.event.TicketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for broadcasting WebSocket messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast ticket event to all connected clients
     */
    public void broadcastTicketEvent(TicketEvent event) {
        log.debug("Broadcasting ticket event: {} - {}", event.getType(), event.getTicket().getTicketNumber());
        messagingTemplate.convertAndSend("/topic/tickets", event);
    }

    /**
     * Send ticket event to specific user
     */
    public void sendTicketEventToUser(String userId, TicketEvent event) {
        log.debug("Sending ticket event to user {}: {} - {}",
                userId, event.getType(), event.getTicket().getTicketNumber());
        messagingTemplate.convertAndSendToUser(userId, "/queue/tickets", event);
    }

    /**
     * Broadcast notification to all users
     */
    public void broadcastNotification(String message) {
        log.debug("Broadcasting notification: {}", message);
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }

    /**
     * Send notification to specific user
     */
    public void sendNotificationToUser(UUID userId, NotificationResponse notification) {
        log.debug("Sending notification to user {}: {}", userId, notification.getTitle());
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                NotificationEvent.created(notification)
        );
    }

    /**
     * Send notification count update to specific user
     */
    public void sendNotificationCountToUser(UUID userId, long count) {
        log.debug("Sending notification count to user {}: {}", userId, count);
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications/count",
                NotificationEvent.countUpdate(count)
        );
    }
}
