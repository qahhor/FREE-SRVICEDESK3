package io.greenwhite.servicedesk.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating/updating SLA holiday
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaHolidayRequest {

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private Boolean recurring;

    @NotNull(message = "Business hours ID is required")
    private UUID businessHoursId;
}
