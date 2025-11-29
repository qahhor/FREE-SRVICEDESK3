package io.greenwhite.servicedesk.ticket.event;

import io.greenwhite.servicedesk.ticket.dto.TicketDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Ticket event for WebSocket broadcasting
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEvent {

    private TicketEventType type;
    private TicketDTO ticket;
    private String message;
    private LocalDateTime timestamp;

    public enum TicketEventType {
        CREATED,
        UPDATED,
        ASSIGNED,
        STATUS_CHANGED,
        COMMENTED,
        DELETED
    }

    public static TicketEvent created(TicketDTO ticket) {
        return TicketEvent.builder()
                .type(TicketEventType.CREATED)
                .ticket(ticket)
                .message("New ticket created: " + ticket.getTicketNumber())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static TicketEvent updated(TicketDTO ticket) {
        return TicketEvent.builder()
                .type(TicketEventType.UPDATED)
                .ticket(ticket)
                .message("Ticket updated: " + ticket.getTicketNumber())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static TicketEvent assigned(TicketDTO ticket) {
        return TicketEvent.builder()
                .type(TicketEventType.ASSIGNED)
                .ticket(ticket)
                .message("Ticket assigned: " + ticket.getTicketNumber())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static TicketEvent statusChanged(TicketDTO ticket) {
        return TicketEvent.builder()
                .type(TicketEventType.STATUS_CHANGED)
                .ticket(ticket)
                .message("Ticket status changed: " + ticket.getTicketNumber())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
