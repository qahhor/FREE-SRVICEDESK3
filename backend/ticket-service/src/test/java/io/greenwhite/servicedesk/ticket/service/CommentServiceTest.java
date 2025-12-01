package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.UserRole;
import io.greenwhite.servicedesk.ticket.dto.CommentRequest;
import io.greenwhite.servicedesk.ticket.dto.CommentResponse;
import io.greenwhite.servicedesk.ticket.model.Comment;
import io.greenwhite.servicedesk.ticket.model.Project;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.CommentRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private CommentService commentService;

    private UUID ticketId;
    private UUID userId;
    private UUID commentId;
    private Ticket testTicket;
    private User testUser;
    private Comment testComment;
    private Project testProject;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        userId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        testProject = Project.builder()
                .key("TEST")
                .name("Test Project")
                .build();
        testProject.setId(UUID.randomUUID());

        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.AGENT)
                .active(true)
                .build();
        testUser.setId(userId);

        testTicket = Ticket.builder()
                .subject("Test Ticket")
                .description("Test Description")
                .project(testProject)
                .requester(testUser)
                .build();
        testTicket.setId(ticketId);
        testTicket.generateTicketNumber(1L);

        testComment = Comment.builder()
                .ticket(testTicket)
                .author(testUser)
                .content("Test comment content")
                .isInternal(false)
                .isAutomated(false)
                .build();
        testComment.setId(commentId);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should add comment successfully")
    void shouldAddCommentSuccessfully() {
        // Given
        CommentRequest request = new CommentRequest();
        request.setContent("New comment content");
        request.setIsInternal(false);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        CommentResponse response = commentService.addComment(ticketId, request, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Test comment content");
        assertThat(response.getTicketId()).isEqualTo(ticketId);

        verify(commentRepository).save(any(Comment.class));
        verify(webSocketService).broadcastTicketEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when ticket not found")
    void shouldThrowExceptionWhenTicketNotFound() {
        // Given
        CommentRequest request = new CommentRequest();
        request.setContent("New comment content");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.addComment(ticketId, request, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ticket not found");

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get comments by ticket ID")
    void shouldGetCommentsByTicketId() {
        // Given
        when(commentRepository.findByTicketId(ticketId)).thenReturn(Arrays.asList(testComment));

        // When
        List<CommentResponse> comments = commentService.getCommentsByTicketId(ticketId, true);

        // Then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("Test comment content");

        verify(commentRepository).findByTicketId(ticketId);
    }

    @Test
    @DisplayName("Should get public comments only when includeInternal is false")
    void shouldGetPublicCommentsOnly() {
        // Given
        when(commentRepository.findPublicCommentsByTicketId(ticketId)).thenReturn(Arrays.asList(testComment));

        // When
        List<CommentResponse> comments = commentService.getCommentsByTicketId(ticketId, false);

        // Then
        assertThat(comments).hasSize(1);

        verify(commentRepository).findPublicCommentsByTicketId(ticketId);
    }

    @Test
    @DisplayName("Should get comment by ID")
    void shouldGetCommentById() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When
        CommentResponse response = commentService.getCommentById(commentId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(commentId);
        assertThat(response.getContent()).isEqualTo("Test comment content");

        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("Should update comment successfully")
    void shouldUpdateCommentSuccessfully() {
        // Given
        CommentRequest request = new CommentRequest();
        request.setContent("Updated comment content");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        CommentResponse response = commentService.updateComment(commentId, request, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(testComment.getContent()).isEqualTo("Updated comment content");

        verify(commentRepository).save(testComment);
        verify(webSocketService).broadcastTicketEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when updating comment by non-author")
    void shouldThrowExceptionWhenUpdatingByNonAuthor() {
        // Given
        CommentRequest request = new CommentRequest();
        request.setContent("Updated comment content");
        UUID differentUserId = UUID.randomUUID();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(commentId, request, differentUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only edit your own comments");

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete comment successfully")
    void shouldDeleteCommentSuccessfully() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When
        commentService.deleteComment(commentId, userId);

        // Then
        assertThat(testComment.getDeleted()).isTrue();
        verify(commentRepository).save(testComment);
    }

    @Test
    @DisplayName("Should count comments for ticket")
    void shouldCountCommentsForTicket() {
        // Given
        when(commentRepository.countByTicketId(ticketId)).thenReturn(5L);

        // When
        Long count = commentService.countComments(ticketId);

        // Then
        assertThat(count).isEqualTo(5L);
        verify(commentRepository).countByTicketId(ticketId);
    }

    @Test
    @DisplayName("Should add system comment")
    void shouldAddSystemComment() {
        // Given
        String systemContent = "System generated message";
        Comment systemComment = Comment.builder()
                .ticket(testTicket)
                .content(systemContent)
                .isInternal(true)
                .isAutomated(true)
                .build();
        systemComment.setId(UUID.randomUUID());
        systemComment.setCreatedAt(LocalDateTime.now());
        systemComment.setUpdatedAt(LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));
        when(commentRepository.save(any(Comment.class))).thenReturn(systemComment);

        // When
        CommentResponse response = commentService.addSystemComment(ticketId, systemContent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getIsAutomated()).isTrue();
        assertThat(response.getIsInternal()).isTrue();

        verify(commentRepository).save(any(Comment.class));
    }
}
