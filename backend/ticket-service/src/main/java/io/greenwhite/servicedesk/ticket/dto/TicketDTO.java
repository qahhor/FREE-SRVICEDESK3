package io.greenwhite.servicedesk.ticket.dto;

import io.greenwhite.servicedesk.common.enums.ChannelType;
import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ticket DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    private UUID id;
    private String ticketNumber;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private ChannelType channel;
    private UUID projectId;
    private String projectName;
    private UUID requesterId;
    private String requesterName;
    private UUID assigneeId;
    private String assigneeName;
    private UUID teamId;
    private String teamName;
    private String category;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime dueDate;
    private Boolean isPublic;
}
