package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.TicketPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for SLA policy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicyResponse {

    private UUID id;

    private String name;

    private String description;

    private Boolean isDefault;

    private Boolean active;

    private SlaBusinessHoursResponse businessHours;

    private List<SlaPriorityResponse> priorities;

    private List<ProjectSummary> projects;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlaPriorityResponse {
        private UUID id;
        private TicketPriority priority;
        private Integer firstResponseMinutes;
        private Integer resolutionMinutes;
        private Integer nextResponseMinutes;
        private Boolean firstResponseEnabled;
        private Boolean resolutionEnabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectSummary {
        private UUID id;
        private String key;
        private String name;
    }
}
