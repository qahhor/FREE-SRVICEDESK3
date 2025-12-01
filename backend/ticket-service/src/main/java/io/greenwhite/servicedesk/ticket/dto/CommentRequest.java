package io.greenwhite.servicedesk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating a comment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotBlank(message = "Comment content cannot be empty")
    @Size(min = 1, max = 10000, message = "Comment must be between 1 and 10000 characters")
    private String content;

    private Boolean isInternal = false;

    private String attachments; // JSON array of file URLs
}
