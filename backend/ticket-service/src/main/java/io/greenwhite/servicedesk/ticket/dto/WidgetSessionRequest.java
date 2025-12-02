package io.greenwhite.servicedesk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for starting a widget session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetSessionRequest {

    @NotBlank(message = "Project key is required")
    @Size(max = 20, message = "Project key must be at most 20 characters")
    private String projectKey;

    @Size(max = 255, message = "Visitor name must be at most 255 characters")
    private String visitorName;

    @Size(max = 255, message = "Visitor email must be at most 255 characters")
    private String visitorEmail;

    private Map<String, String> visitorMetadata;
}
