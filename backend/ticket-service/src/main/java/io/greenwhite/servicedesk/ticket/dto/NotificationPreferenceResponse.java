package io.greenwhite.servicedesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for notification preferences
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceResponse {

    private UUID id;
    private UUID userId;
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean pushEnabled;
    
    private Map<String, Boolean> emailSettings;
    private Map<String, Boolean> inAppSettings;
    
    private Boolean quietHoursEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private String timezone;
}
