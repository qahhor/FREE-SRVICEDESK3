package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.SlaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for SLA metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaMetricsResponse {

    // Dashboard metrics
    private long totalTicketsWithSla;
    private long ticketsOnTrack;
    private long ticketsInWarning;
    private long ticketsBreached;

    private double firstResponseComplianceRate;
    private double resolutionComplianceRate;
    private double overallComplianceRate;

    private long averageFirstResponseMinutes;
    private long averageResolutionMinutes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketSlaStatus {
        private UUID ticketId;
        private String ticketNumber;
        private String subject;
        private SlaStatus firstResponseStatus;
        private SlaStatus resolutionStatus;
        private LocalDateTime slaFirstResponseDue;
        private LocalDateTime slaResolutionDue;
        private Integer firstResponseMinutesRemaining;
        private Integer resolutionMinutesRemaining;
        private Boolean firstResponseBreached;
        private Boolean resolutionBreached;
    }
}
