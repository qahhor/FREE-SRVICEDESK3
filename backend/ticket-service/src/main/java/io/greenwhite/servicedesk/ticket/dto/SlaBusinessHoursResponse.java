package io.greenwhite.servicedesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for SLA business hours
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaBusinessHoursResponse {

    private UUID id;

    private String name;

    private String timezone;

    private String schedule;

    private List<SlaHolidayResponse> holidays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlaHolidayResponse {
        private UUID id;
        private String name;
        private LocalDate date;
        private Boolean recurring;
    }
}
