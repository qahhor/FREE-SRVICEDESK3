package io.greenwhite.servicedesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for comment data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private UUID id;
    private UUID ticketId;
    private String ticketNumber;
    private AuthorInfo author;
    private String content;
    private Boolean isInternal;
    private Boolean isAutomated;
    private String attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Nested author information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private UUID id;
        private String username;
        private String email;
        private String fullName;
        private String role;
    }
}
