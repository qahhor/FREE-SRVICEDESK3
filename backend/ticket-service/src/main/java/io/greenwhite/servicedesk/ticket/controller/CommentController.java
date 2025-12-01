package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.common.dto.ApiResponse;
import io.greenwhite.servicedesk.ticket.dto.CommentRequest;
import io.greenwhite.servicedesk.ticket.dto.CommentResponse;
import io.greenwhite.servicedesk.ticket.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing ticket comments
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {

    private final CommentService commentService;

    /**
     * Add a comment to a ticket
     */
    @PostMapping("/tickets/{ticketId}/comments")
    @Operation(summary = "Add comment to ticket", description = "Creates a new comment on the specified ticket")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {

        log.info("POST /api/v1/tickets/{}/comments - Adding comment", ticketId);

        UUID userId = UUID.fromString(authentication.getName());
        CommentResponse comment = commentService.addComment(ticketId, request, userId);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Comment added successfully", comment));
    }

    /**
     * Get all comments for a ticket
     */
    @GetMapping("/tickets/{ticketId}/comments")
    @Operation(summary = "Get ticket comments", description = "Retrieves all comments for a specific ticket")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable UUID ticketId,
            @RequestParam(defaultValue = "true") boolean includeInternal,
            Authentication authentication) {

        log.info("GET /api/v1/tickets/{}/comments - Fetching comments", ticketId);

        // TODO: Check if user has permission to see internal comments
        List<CommentResponse> comments = commentService.getCommentsByTicketId(ticketId, includeInternal);

        return ResponseEntity.ok(
            ApiResponse.success("Comments retrieved successfully", comments)
        );
    }

    /**
     * Get a specific comment by ID
     */
    @GetMapping("/comments/{commentId}")
    @Operation(summary = "Get comment by ID", description = "Retrieves a specific comment")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(
            @PathVariable UUID commentId) {

        log.info("GET /api/v1/comments/{} - Fetching comment", commentId);

        CommentResponse comment = commentService.getCommentById(commentId);

        return ResponseEntity.ok(
            ApiResponse.success("Comment retrieved successfully", comment)
        );
    }

    /**
     * Update a comment
     */
    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Update comment", description = "Updates an existing comment")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {

        log.info("PUT /api/v1/comments/{} - Updating comment", commentId);

        UUID userId = UUID.fromString(authentication.getName());
        CommentResponse comment = commentService.updateComment(commentId, request, userId);

        return ResponseEntity.ok(
            ApiResponse.success("Comment updated successfully", comment)
        );
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete comment", description = "Soft deletes a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID commentId,
            Authentication authentication) {

        log.info("DELETE /api/v1/comments/{} - Deleting comment", commentId);

        UUID userId = UUID.fromString(authentication.getName());
        commentService.deleteComment(commentId, userId);

        return ResponseEntity.ok(
            ApiResponse.success("Comment deleted successfully", (Void) null)
        );
    }

    /**
     * Get comment count for a ticket
     */
    @GetMapping("/tickets/{ticketId}/comments/count")
    @Operation(summary = "Count comments", description = "Returns the number of comments on a ticket")
    public ResponseEntity<ApiResponse<Long>> getCommentCount(@PathVariable UUID ticketId) {

        log.info("GET /api/v1/tickets/{}/comments/count - Counting comments", ticketId);

        Long count = commentService.countComments(ticketId);

        return ResponseEntity.ok(
            ApiResponse.success("Comment count retrieved successfully", count)
        );
    }
}
