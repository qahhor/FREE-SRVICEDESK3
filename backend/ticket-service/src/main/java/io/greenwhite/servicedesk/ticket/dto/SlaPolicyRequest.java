package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.TicketPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating/updating SLA policy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicyRequest {

    @NotNull(message = "Name is required")
    private String name;

    private String description;

    private Boolean isDefault;

    private Boolean active;

    private UUID businessHoursId;

    private List<UUID> projectIds;

    private List<SlaPriorityRequest> priorities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlaPriorityRequest {
        @NotNull(message = "Priority is required")
        private TicketPriority priority;

        private Integer firstResponseMinutes;

        private Integer resolutionMinutes;

        private Integer nextResponseMinutes;

        private Boolean firstResponseEnabled;

        private Boolean resolutionEnabled;
    }
}
