package io.greenwhite.servicedesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for widget configuration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetConfigResponse {

    private String projectKey;
    private String projectName;
    private boolean online;
    private String greeting;
    private String offlineMessage;
    private String primaryColor;
    private int maxFileSize;
    private String[] allowedFileTypes;
}
