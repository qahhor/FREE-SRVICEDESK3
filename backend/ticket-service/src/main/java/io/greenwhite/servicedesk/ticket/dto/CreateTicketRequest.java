package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.ChannelType;
import io.greenwhite.servicedesk.common.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Create ticket request DTO
 */
@Data
public class CreateTicketRequest {

    @NotBlank(message = "Subject is required")
    private String subject;

    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotNull(message = "Channel is required")
    private ChannelType channel;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID assigneeId;

    private UUID teamId;

    private String category;

    private String tags;
}
