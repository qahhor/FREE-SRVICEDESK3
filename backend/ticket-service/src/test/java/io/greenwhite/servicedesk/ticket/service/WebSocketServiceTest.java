package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.ticket.dto.TicketDTO;
import io.greenwhite.servicedesk.ticket.event.TicketEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketService Tests")
class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketService webSocketService;

    private TicketDTO testTicketDTO;
    private TicketEvent testEvent;

    @BeforeEach
    void setUp() {
        testTicketDTO = TicketDTO.builder()
                .id(UUID.randomUUID())
                .ticketNumber("TEST-1")
                .subject("Test Ticket")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.HIGH)
                .createdAt(LocalDateTime.now())
                .build();

        testEvent = TicketEvent.builder()
                .type(TicketEvent.TicketEventType.CREATED)
                .ticket(testTicketDTO)
                .message("Test event")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should broadcast ticket event")
    void shouldBroadcastTicketEvent() {
        // When
        webSocketService.broadcastTicketEvent(testEvent);

        // Then
        verify(messagingTemplate).convertAndSend("/topic/tickets", testEvent);
    }

    @Test
    @DisplayName("Should send ticket event to specific user")
    void shouldSendTicketEventToUser() {
        // Given
        String userId = UUID.randomUUID().toString();

        // When
        webSocketService.sendTicketEventToUser(userId, testEvent);

        // Then
        verify(messagingTemplate).convertAndSendToUser(userId, "/queue/tickets", testEvent);
    }

    @Test
    @DisplayName("Should broadcast notification")
    void shouldBroadcastNotification() {
        // Given
        String message = "Test notification";

        // When
        webSocketService.broadcastNotification(message);

        // Then
        verify(messagingTemplate).convertAndSend("/topic/notifications", message);
    }
}
