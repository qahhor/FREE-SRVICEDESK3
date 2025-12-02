package io.greenwhite.servicedesk.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating SLA business hours
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaBusinessHoursRequest {

    @NotNull(message = "Name is required")
    private String name;

    private String timezone;

    /**
     * JSON schedule in format: {"monday": {"start": "09:00", "end": "18:00"}, ...}
     */
    @NotNull(message = "Schedule is required")
    private String schedule;
}
