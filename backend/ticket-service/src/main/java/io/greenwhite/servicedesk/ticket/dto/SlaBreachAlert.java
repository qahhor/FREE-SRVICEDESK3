package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.EscalationAction;
import io.greenwhite.servicedesk.common.enums.EscalationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for SLA breach alert
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaBreachAlert {

    private UUID ticketId;

    private String ticketNumber;

    private String subject;

    private EscalationType breachType;

    private LocalDateTime dueAt;

    private Integer minutesUntilBreach;

    private boolean breached;

    private UUID escalationId;

    private String escalationName;

    private EscalationAction escalationAction;

    private LocalDateTime alertTime;
}
