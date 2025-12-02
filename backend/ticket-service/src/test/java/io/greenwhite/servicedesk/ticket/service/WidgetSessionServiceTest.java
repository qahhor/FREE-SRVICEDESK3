package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.*;
import io.greenwhite.servicedesk.ticket.model.*;
import io.greenwhite.servicedesk.ticket.model.WidgetSession.WidgetSessionStatus;
import io.greenwhite.servicedesk.ticket.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WidgetSessionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WidgetSessionService Tests")
class WidgetSessionServiceTest {

    @Mock
    private WidgetSessionRepository sessionRepository;

    @Mock
    private WidgetMessageRepository messageRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private WidgetSessionService widgetSessionService;

    private UUID projectId;
    private Project testProject;
    private WidgetSession testSession;
    private User testUser;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();

        testProject = Project.builder()
                .key("TEST")
                .name("Test Project")
                .build();
        testProject.setId(projectId);

        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
        testUser.setId(UUID.randomUUID());

        testSession = WidgetSession.builder()
                .sessionToken("test-token-123")
                .visitorName("Test Visitor")
                .visitorEmail("visitor@example.com")
                .project(testProject)
                .status(WidgetSessionStatus.ACTIVE)
                .build();
        testSession.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should start session successfully")
    void shouldStartSessionSuccessfully() {
        // Given
        WidgetSessionRequest request = WidgetSessionRequest.builder()
                .projectKey("TEST")
                .visitorName("Test Visitor")
                .visitorEmail("visitor@example.com")
                .build();

        when(projectRepository.findByKey("TEST")).thenReturn(Optional.of(testProject));
        when(sessionRepository.save(any(WidgetSession.class))).thenAnswer(invocation -> {
            WidgetSession session = invocation.getArgument(0);
            session.setId(UUID.randomUUID());
            return session;
        });

        // When
        WidgetSessionResponse response = widgetSessionService.startSession(request, "127.0.0.1", "Mozilla/5.0");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getVisitorName()).isEqualTo("Test Visitor");
        assertThat(response.getVisitorEmail()).isEqualTo("visitor@example.com");
        assertThat(response.getProjectId()).isEqualTo(projectId);
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getSessionToken()).isNotNull();

        verify(sessionRepository).save(any(WidgetSession.class));
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
        // Given
        WidgetSessionRequest request = WidgetSessionRequest.builder()
                .projectKey("INVALID")
                .build();

        when(projectRepository.findByKey("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> widgetSessionService.startSession(request, "127.0.0.1", "Mozilla/5.0"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get session by ID")
    void shouldGetSessionById() {
        // Given
        UUID sessionId = testSession.getId();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));

        // When
        WidgetSessionResponse response = widgetSessionService.getSession(sessionId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(sessionId);
        assertThat(response.getVisitorName()).isEqualTo("Test Visitor");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent session")
    void shouldThrowExceptionWhenGettingNonExistentSession() {
        // Given
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> widgetSessionService.getSession(sessionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Session not found");
    }

    @Test
    @DisplayName("Should send visitor message successfully")
    void shouldSendVisitorMessageSuccessfully() {
        // Given
        UUID sessionId = testSession.getId();
        WidgetMessageRequest request = WidgetMessageRequest.builder()
                .content("Hello, I need help!")
                .messageType("TEXT")
                .build();

        Ticket mockTicket = Ticket.builder()
                .subject("Test Ticket")
                .project(testProject)
                .requester(testUser)
                .build();
        mockTicket.setId(UUID.randomUUID());
        mockTicket.setTicketNumber("TEST-1");

        when(sessionRepository.findByIdAndStatus(sessionId, WidgetSessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));
        when(messageRepository.save(any(WidgetMessage.class))).thenAnswer(invocation -> {
            WidgetMessage msg = invocation.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(userRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(testUser));
        when(ticketRepository.count()).thenReturn(0L);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);
        when(sessionRepository.save(any(WidgetSession.class))).thenReturn(testSession);

        // When
        WidgetMessageResponse response = widgetSessionService.sendVisitorMessage(
                sessionId, request, "test-token-123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hello, I need help!");
        assertThat(response.getSenderType()).isEqualTo("VISITOR");
        assertThat(response.getMessageType()).isEqualTo("TEXT");

        verify(messageRepository).save(any(WidgetMessage.class));
    }

    @Test
    @DisplayName("Should throw exception when sending message with invalid token")
    void shouldThrowExceptionWhenSendingMessageWithInvalidToken() {
        // Given
        UUID sessionId = testSession.getId();
        WidgetMessageRequest request = WidgetMessageRequest.builder()
                .content("Hello")
                .build();

        when(sessionRepository.findByIdAndStatus(sessionId, WidgetSessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));

        // When & Then
        assertThatThrownBy(() -> widgetSessionService.sendVisitorMessage(
                sessionId, request, "wrong-token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid session token");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should close session successfully")
    void shouldCloseSessionSuccessfully() {
        // Given
        UUID sessionId = testSession.getId();

        when(sessionRepository.findByIdAndStatus(sessionId, WidgetSessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(WidgetSession.class))).thenReturn(testSession);
        when(messageRepository.save(any(WidgetMessage.class))).thenAnswer(invocation -> {
            WidgetMessage msg = invocation.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });

        // When
        widgetSessionService.closeSession(sessionId, "test-token-123");

        // Then
        verify(sessionRepository).save(any(WidgetSession.class));
        verify(messageRepository).save(any(WidgetMessage.class));
        assertThat(testSession.getStatus()).isEqualTo(WidgetSessionStatus.CLOSED);
    }

    @Test
    @DisplayName("Should get widget config for valid project")
    void shouldGetWidgetConfigForValidProject() {
        // Given
        when(projectRepository.findByKey("TEST")).thenReturn(Optional.of(testProject));

        // When
        WidgetConfigResponse config = widgetSessionService.getConfig("TEST");

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getProjectKey()).isEqualTo("TEST");
        assertThat(config.getProjectName()).isEqualTo("Test Project");
        assertThat(config.isOnline()).isTrue();
        assertThat(config.getMaxFileSize()).isEqualTo(10 * 1024 * 1024);
    }
}
