package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.ChannelType;
import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.CreateTicketRequest;
import io.greenwhite.servicedesk.ticket.dto.TicketDTO;
import io.greenwhite.servicedesk.ticket.event.TicketEvent;
import io.greenwhite.servicedesk.ticket.model.Project;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.ProjectRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Unit tests for TicketService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService Tests")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private TicketService ticketService;

    private UUID projectId;
    private UUID userId;
    private Project testProject;
    private User testUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

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
        testUser.setId(userId);

        testTicket = Ticket.builder()
                .subject("Test Ticket")
                .description("Test Description")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.MEDIUM)
                .channel(ChannelType.WEB_FORM)
                .project(testProject)
                .requester(testUser)
                .build();
        testTicket.setId(UUID.randomUUID());
        testTicket.generateTicketNumber(1L);
    }

    @Test
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicketSuccessfully() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest();
        request.setSubject("Test Ticket");
        request.setDescription("Test Description");
        request.setPriority(TicketPriority.MEDIUM);
        request.setChannel(ChannelType.WEB_FORM);
        request.setProjectId(projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(ticketRepository.count()).thenReturn(0L);

        // When
        TicketDTO result = ticketService.createTicket(request, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo("Test Ticket");
        assertThat(result.getStatus()).isEqualTo(TicketStatus.NEW);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.MEDIUM);

        verify(ticketRepository).save(any(Ticket.class));
        verify(webSocketService).broadcastTicketEvent(any(TicketEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest();
        request.setProjectId(projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.createTicket(request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");

        verify(ticketRepository, never()).save(any());
        verify(webSocketService, never()).broadcastTicketEvent(any());
    }

    @Test
    @DisplayName("Should update ticket status successfully")
    void shouldUpdateTicketStatusSuccessfully() {
        // Given
        UUID ticketId = testTicket.getId();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        TicketDTO result = ticketService.updateTicketStatus(ticketId, TicketStatus.RESOLVED);

        // Then
        assertThat(result).isNotNull();
        assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(testTicket.getResolvedAt()).isNotNull();

        verify(ticketRepository).save(testTicket);
        verify(webSocketService).broadcastTicketEvent(any(TicketEvent.class));
    }

    @Test
    @DisplayName("Should assign ticket to user successfully")
    void shouldAssignTicketSuccessfully() {
        // Given
        UUID ticketId = testTicket.getId();
        UUID assigneeId = UUID.randomUUID();
        User assignee = User.builder()
                .email("assignee@example.com")
                .firstName("Assignee")
                .lastName("User")
                .build();
        assignee.setId(assigneeId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        TicketDTO result = ticketService.assignTicket(ticketId, assigneeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(testTicket.getAssignee()).isEqualTo(assignee);
        assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.OPEN);

        verify(ticketRepository).save(testTicket);
        verify(webSocketService).broadcastTicketEvent(any(TicketEvent.class));
    }

    @Test
    @DisplayName("Should get ticket by ID successfully")
    void shouldGetTicketByIdSuccessfully() {
        // Given
        UUID ticketId = testTicket.getId();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(testTicket));

        // When
        TicketDTO result = ticketService.getTicket(ticketId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTicketNumber()).isEqualTo(testTicket.getTicketNumber());
        assertThat(result.getSubject()).isEqualTo(testTicket.getSubject());

        verify(ticketRepository).findById(ticketId);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent ticket")
    void shouldThrowExceptionWhenGettingNonExistentTicket() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.getTicket(ticketId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }
}
