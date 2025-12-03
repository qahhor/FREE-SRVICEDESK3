package io.greenwhite.servicedesk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for push subscription
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscriptionRequest {

    @NotBlank(message = "Endpoint is required")
    private String endpoint;

    @NotBlank(message = "P256DH key is required")
    private String p256dhKey;

    @NotBlank(message = "Auth key is required")
    private String authKey;

    private String userAgent;
}
