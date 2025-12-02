package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.EscalationAction;
import io.greenwhite.servicedesk.common.enums.EscalationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating/updating SLA escalation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaEscalationRequest {

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Policy ID is required")
    private UUID policyId;

    @NotNull(message = "Escalation type is required")
    private EscalationType type;

    @NotNull(message = "Trigger minutes before is required")
    private Integer triggerMinutesBefore;

    @NotNull(message = "Action is required")
    private EscalationAction action;

    private Set<UUID> notifyUserIds;

    private UUID reassignToUserId;

    private Boolean active;
}
