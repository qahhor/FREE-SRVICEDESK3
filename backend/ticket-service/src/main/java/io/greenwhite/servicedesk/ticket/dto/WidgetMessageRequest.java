package io.greenwhite.servicedesk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for sending a widget message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = 10000, message = "Message content must be at most 10000 characters")
    private String content;

    private String messageType; // TEXT, FILE, IMAGE

    private UUID attachmentId;
}
