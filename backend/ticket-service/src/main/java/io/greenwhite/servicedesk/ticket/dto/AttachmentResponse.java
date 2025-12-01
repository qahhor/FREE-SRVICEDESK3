package io.greenwhite.servicedesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for attachment data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private UUID id;
    private UUID ticketId;
    private UUID commentId;
    private UUID uploadedBy;
    private String uploaderName;
    private String originalFilename;
    private String storedFilename;
    private String contentType;
    private Long fileSize;
    private String formattedFileSize;
    private String downloadUrl;
    private Boolean isImage;
    private String storageType;
    private LocalDateTime createdAt;
}
