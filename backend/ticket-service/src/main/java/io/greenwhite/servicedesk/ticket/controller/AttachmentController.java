package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.common.dto.ApiResponse;
import io.greenwhite.servicedesk.ticket.dto.AttachmentResponse;
import io.greenwhite.servicedesk.ticket.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for file attachment management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "File attachment management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AttachmentController {

    private final FileStorageService fileStorageService;

    /**
     * Upload file to a ticket
     */
    @PostMapping("/tickets/{ticketId}/attachments")
    @Operation(summary = "Upload file to ticket", description = "Uploads a file and attaches it to a ticket")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadFileToTicket(
            @PathVariable UUID ticketId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("POST /api/v1/tickets/{}/attachments - Uploading file: {}", ticketId, file.getOriginalFilename());

        UUID userId = UUID.fromString(authentication.getName());
        AttachmentResponse attachment = fileStorageService.storeFileForTicket(file, ticketId, userId);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(attachment, "File uploaded successfully"));
    }

    /**
     * Upload file to a comment
     */
    @PostMapping("/comments/{commentId}/attachments")
    @Operation(summary = "Upload file to comment", description = "Uploads a file and attaches it to a comment")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadFileToComment(
            @PathVariable UUID commentId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("POST /api/v1/comments/{}/attachments - Uploading file: {}", commentId, file.getOriginalFilename());

        UUID userId = UUID.fromString(authentication.getName());
        AttachmentResponse attachment = fileStorageService.storeFileForComment(file, commentId, userId);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(attachment, "File uploaded successfully"));
    }

    /**
     * Get all attachments for a ticket
     */
    @GetMapping("/tickets/{ticketId}/attachments")
    @Operation(summary = "Get ticket attachments", description = "Retrieves all attachments for a specific ticket")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getTicketAttachments(
            @PathVariable UUID ticketId) {

        log.info("GET /api/v1/tickets/{}/attachments - Fetching attachments", ticketId);

        List<AttachmentResponse> attachments = fileStorageService.getTicketAttachments(ticketId);

        return ResponseEntity.ok(
            ApiResponse.success(attachments, "Attachments retrieved successfully")
        );
    }

    /**
     * Get all attachments for a comment
     */
    @GetMapping("/comments/{commentId}/attachments")
    @Operation(summary = "Get comment attachments", description = "Retrieves all attachments for a specific comment")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getCommentAttachments(
            @PathVariable UUID commentId) {

        log.info("GET /api/v1/comments/{}/attachments - Fetching attachments", commentId);

        List<AttachmentResponse> attachments = fileStorageService.getCommentAttachments(commentId);

        return ResponseEntity.ok(
            ApiResponse.success(attachments, "Attachments retrieved successfully")
        );
    }

    /**
     * Download an attachment
     */
    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(summary = "Download attachment", description = "Downloads a specific attachment file")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable UUID attachmentId,
            HttpServletRequest request) {

        log.info("GET /api/v1/attachments/{}/download - Downloading file", attachmentId);

        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(attachmentId);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    /**
     * Delete an attachment
     */
    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Delete attachment", description = "Soft deletes an attachment")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable UUID attachmentId,
            Authentication authentication) {

        log.info("DELETE /api/v1/attachments/{} - Deleting attachment", attachmentId);

        UUID userId = UUID.fromString(authentication.getName());
        fileStorageService.deleteAttachment(attachmentId, userId);

        return ResponseEntity.ok(
            ApiResponse.success(null, "Attachment deleted successfully")
        );
    }

    /**
     * Upload multiple files to a ticket
     */
    @PostMapping("/tickets/{ticketId}/attachments/batch")
    @Operation(summary = "Upload multiple files to ticket", description = "Uploads multiple files to a ticket at once")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadMultipleFilesToTicket(
            @PathVariable UUID ticketId,
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {

        log.info("POST /api/v1/tickets/{}/attachments/batch - Uploading {} files", ticketId, files.length);

        UUID userId = UUID.fromString(authentication.getName());
        List<AttachmentResponse> attachments = java.util.Arrays.stream(files)
            .map(file -> fileStorageService.storeFileForTicket(file, ticketId, userId))
            .collect(java.util.stream.Collectors.toList());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(attachments, String.format("%d files uploaded successfully", files.length)));
    }
}
