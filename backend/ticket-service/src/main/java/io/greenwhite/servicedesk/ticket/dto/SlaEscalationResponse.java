package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.EscalationAction;
import io.greenwhite.servicedesk.common.enums.EscalationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for SLA escalation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaEscalationResponse {

    private UUID id;

    private String name;

    private UUID policyId;

    private EscalationType type;

    private Integer triggerMinutesBefore;

    private EscalationAction action;

    private Set<UserSummary> notifyUsers;

    private UserSummary reassignTo;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummary {
        private UUID id;
        private String email;
        private String fullName;
    }
}
