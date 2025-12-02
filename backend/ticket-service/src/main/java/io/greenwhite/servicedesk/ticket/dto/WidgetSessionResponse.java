package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.ticket.model.WidgetSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for widget session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetSessionResponse {

    private UUID id;
    private String sessionToken;
    private String visitorName;
    private String visitorEmail;
    private UUID ticketId;
    private UUID projectId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Create response from entity
     */
    public static WidgetSessionResponse fromEntity(WidgetSession session) {
        return WidgetSessionResponse.builder()
                .id(session.getId())
                .sessionToken(session.getSessionToken())
                .visitorName(session.getVisitorName())
                .visitorEmail(session.getVisitorEmail())
                .ticketId(session.getTicket() != null ? session.getTicket().getId() : null)
                .projectId(session.getProject().getId())
                .status(session.getStatus().name())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
