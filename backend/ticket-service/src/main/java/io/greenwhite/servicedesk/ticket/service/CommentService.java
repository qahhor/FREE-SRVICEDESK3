package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.ticket.dto.CommentRequest;
import io.greenwhite.servicedesk.ticket.dto.CommentResponse;
import io.greenwhite.servicedesk.ticket.event.TicketEvent;
import io.greenwhite.servicedesk.ticket.model.Comment;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.CommentRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing comments on tickets
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;

    /**
     * Add a comment to a ticket
     */
    @Transactional
    public CommentResponse addComment(UUID ticketId, CommentRequest request, UUID authorId) {
        log.info("Adding comment to ticket {} by user {}", ticketId, authorId);

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + authorId));

        Comment comment = Comment.builder()
            .ticket(ticket)
            .author(author)
            .content(request.getContent())
            .isInternal(request.getIsInternal() != null ? request.getIsInternal() : false)
            .isAutomated(false)
            .attachments(request.getAttachments())
            .build();

        comment = commentRepository.save(comment);

        // Update ticket's updated timestamp
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        CommentResponse response = mapToResponse(comment);

        // Broadcast comment event via WebSocket
        webSocketService.broadcastTicketEvent(TicketEvent.builder()
            .type(TicketEvent.TicketEventType.COMMENTED)
            .ticketId(ticket.getId())
            .message("New comment added to ticket " + ticket.getTicketNumber())
            .timestamp(LocalDateTime.now())
            .build());

        log.info("Comment added successfully: {}", comment.getId());
        return response;
    }

    /**
     * Get all comments for a ticket
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicketId(UUID ticketId, boolean includeInternal) {
        log.debug("Fetching comments for ticket {}, includeInternal={}", ticketId, includeInternal);

        List<Comment> comments;
        if (includeInternal) {
            comments = commentRepository.findByTicketId(ticketId);
        } else {
            comments = commentRepository.findPublicCommentsByTicketId(ticketId);
        }

        return comments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific comment by ID
     */
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        return mapToResponse(comment);
    }

    /**
     * Update a comment
     */
    @Transactional
    public CommentResponse updateComment(UUID commentId, CommentRequest request, UUID userId) {
        log.info("Updating comment {} by user {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // Check if user is the author
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        if (request.getIsInternal() != null) {
            comment.setIsInternal(request.getIsInternal());
        }
        if (request.getAttachments() != null) {
            comment.setAttachments(request.getAttachments());
        }

        comment = commentRepository.save(comment);

        // Broadcast update event
        webSocketService.broadcastTicketEvent(TicketEvent.builder()
            .type(TicketEvent.TicketEventType.COMMENTED)
            .ticketId(comment.getTicket().getId())
            .message("Comment updated on ticket " + comment.getTicket().getTicketNumber())
            .timestamp(LocalDateTime.now())
            .build());

        log.info("Comment updated successfully: {}", commentId);
        return mapToResponse(comment);
    }

    /**
     * Delete a comment (soft delete)
     */
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        log.info("Deleting comment {} by user {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // Check if user is the author or admin
        if (!comment.getAuthor().getId().equals(userId)) {
            // TODO: Check if user has ADMIN role
            throw new RuntimeException("You can only delete your own comments");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);

        log.info("Comment deleted successfully: {}", commentId);
    }

    /**
     * Add a system-generated comment
     */
    @Transactional
    public CommentResponse addSystemComment(UUID ticketId, String content) {
        log.info("Adding system comment to ticket {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        Comment comment = Comment.builder()
            .ticket(ticket)
            .content(content)
            .isInternal(true)
            .isAutomated(true)
            .build();

        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }

    /**
     * Count comments for a ticket
     */
    @Transactional(readOnly = true)
    public Long countComments(UUID ticketId) {
        return commentRepository.countByTicketId(ticketId);
    }

    /**
     * Map Comment entity to CommentResponse DTO
     */
    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse.AuthorInfo authorInfo = null;

        if (comment.getAuthor() != null) {
            User author = comment.getAuthor();
            authorInfo = CommentResponse.AuthorInfo.builder()
                .id(author.getId())
                .username(author.getEmail())
                .email(author.getEmail())
                .fullName(author.getFullName())
                .role(author.getRole() != null ? author.getRole().name() : null)
                .build();
        }

        return CommentResponse.builder()
            .id(comment.getId())
            .ticketId(comment.getTicket().getId())
            .ticketNumber(comment.getTicket().getTicketNumber())
            .author(authorInfo)
            .content(comment.getContent())
            .isInternal(comment.getIsInternal())
            .isAutomated(comment.getIsAutomated())
            .attachments(comment.getAttachments())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }
}
