package io.greenwhite.servicedesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comment DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private UUID id;
    private UUID ticketId;
    private UUID authorId;
    private String authorName;
    private String content;
    private Boolean isInternal;
    private Boolean isAutomated;
    private String attachments;
    private LocalDateTime createdAt;
}
