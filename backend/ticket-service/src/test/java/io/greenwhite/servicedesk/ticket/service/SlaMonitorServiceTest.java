package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.SlaStatus;
import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.ticket.dto.SlaMetricsResponse;
import io.greenwhite.servicedesk.ticket.model.Project;
import io.greenwhite.servicedesk.ticket.model.SlaPolicy;
import io.greenwhite.servicedesk.ticket.model.SlaPriority;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.repository.SlaPolicyRepository;
import io.greenwhite.servicedesk.ticket.repository.SlaPriorityRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SlaMonitorService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SlaMonitorService Tests")
class SlaMonitorServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SlaPolicyRepository policyRepository;

    @Mock
    private SlaPriorityRepository priorityRepository;

    @Mock
    private SlaCalculatorService calculatorService;

    @InjectMocks
    private SlaMonitorService monitorService;

    private SlaPolicy slaPolicy;
    private SlaPriority slaPriority;
    private Ticket ticket;
    private Project project;

    @BeforeEach
    void setUp() {
        UUID policyId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        slaPolicy = SlaPolicy.builder()
                .name("Standard SLA")
                .isDefault(true)
                .active(true)
                .build();
        slaPolicy.setId(policyId);

        slaPriority = SlaPriority.builder()
                .policy(slaPolicy)
                .priority(TicketPriority.MEDIUM)
                .firstResponseMinutes(60)
                .resolutionMinutes(480)
                .firstResponseEnabled(true)
                .resolutionEnabled(true)
                .build();
        slaPriority.setId(UUID.randomUUID());

        project = Project.builder()
                .key("TEST")
                .name("Test Project")
                .build();
        project.setId(projectId);

        ticket = Ticket.builder()
                .ticketNumber("TEST-1")
                .subject("Test Ticket")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.MEDIUM)
                .project(project)
                .build();
        ticket.setId(UUID.randomUUID());
        ticket.setCreatedAt(LocalDateTime.now().minusMinutes(30));
    }

    @Test
    @DisplayName("Should return NOT_APPLICABLE when ticket has no SLA policy")
    void shouldReturnNotApplicableWhenNoSlaPolicy() {
        // Given
        ticket.setSlaPolicy(null);

        // When
        SlaStatus status = monitorService.checkTicketSla(ticket);

        // Then
        assertThat(status).isEqualTo(SlaStatus.NOT_APPLICABLE);
    }

    @Test
    @DisplayName("Should return ON_TRACK when within SLA")
    void shouldReturnOnTrackWhenWithinSla() {
        // Given
        ticket.setSlaPolicy(slaPolicy);
        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(60));
        ticket.setSlaResolutionDue(LocalDateTime.now().plusHours(8));
        ticket.setSlaFirstResponseBreached(false);
        ticket.setSlaResolutionBreached(false);

        // When
        SlaStatus status = monitorService.checkTicketSla(ticket);

        // Then
        assertThat(status).isEqualTo(SlaStatus.ON_TRACK);
    }

    @Test
    @DisplayName("Should return BREACHED when first response SLA breached")
    void shouldReturnBreachedWhenFirstResponseBreached() {
        // Given
        ticket.setSlaPolicy(slaPolicy);
        ticket.setSlaFirstResponseDue(LocalDateTime.now().minusMinutes(10));
        ticket.setFirstResponseAt(null);
        ticket.setSlaFirstResponseBreached(false);
        ticket.setSlaResolutionBreached(false);

        // When
        SlaStatus status = monitorService.checkTicketSla(ticket);

        // Then
        assertThat(status).isEqualTo(SlaStatus.BREACHED);
    }

    @Test
    @DisplayName("Should return BREACHED when resolution SLA breached")
    void shouldReturnBreachedWhenResolutionBreached() {
        // Given
        ticket.setSlaPolicy(slaPolicy);
        ticket.setFirstResponseAt(LocalDateTime.now().minusMinutes(30));
        ticket.setSlaResolutionDue(LocalDateTime.now().minusMinutes(10));
        ticket.setSlaFirstResponseBreached(false);
        ticket.setSlaResolutionBreached(true);

        // When
        SlaStatus status = monitorService.checkTicketSla(ticket);

        // Then
        assertThat(status).isEqualTo(SlaStatus.BREACHED);
    }

    @Test
    @DisplayName("Should return PAUSED when ticket status is PENDING")
    void shouldReturnPausedWhenStatusIsPending() {
        // Given
        ticket.setSlaPolicy(slaPolicy);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setSlaPausedAt(LocalDateTime.now());
        ticket.setSlaFirstResponseBreached(false);
        ticket.setSlaResolutionBreached(false);

        // When
        SlaStatus status = monitorService.checkTicketSla(ticket);

        // Then
        assertThat(status).isEqualTo(SlaStatus.PAUSED);
    }

    @Test
    @DisplayName("Should return WARNING when approaching first response breach")
    void shouldReturnWarningWhenApproachingFirstResponseBreach() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ticket.setSlaPolicy(slaPolicy);
        ticket.setCreatedAt(now.minusMinutes(50));
        ticket.setSlaFirstResponseDue(now.plusMinutes(10)); // 50 minutes used out of 60 (83%)
        ticket.setSlaResolutionDue(now.plusHours(8));
        ticket.setFirstResponseAt(null);
        ticket.setSlaFirstResponseBreached(false);
        ticket.setSlaResolutionBreached(false);

        // When
        SlaStatus status = monitorService.checkTicketSla(ticket);

        // Then
        assertThat(status).isEqualTo(SlaStatus.WARNING);
    }

    @Test
    @DisplayName("Should get tickets approaching breach")
    void shouldGetTicketsApproachingBreach() {
        // Given
        ticket.setSlaPolicy(slaPolicy);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(30));
        ticket.setSlaFirstResponseBreached(false);

        when(ticketRepository.findTicketsApproachingSla(any(), any()))
                .thenReturn(List.of(ticket));

        // When
        List<Ticket> approachingBreach = monitorService.getTicketsApproachingBreach(60);

        // Then
        assertThat(approachingBreach).hasSize(1);
        assertThat(approachingBreach.get(0).getTicketNumber()).isEqualTo("TEST-1");
    }

    @Test
    @DisplayName("Should get breached tickets")
    void shouldGetBreachedTickets() {
        // Given
        ticket.setSlaPolicy(slaPolicy);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setSlaFirstResponseDue(LocalDateTime.now().minusMinutes(10));
        ticket.setFirstResponseAt(null);
        ticket.setSlaFirstResponseBreached(false);

        when(ticketRepository.findBreachedTickets(any(), any()))
                .thenReturn(List.of(ticket));

        // When
        List<Ticket> breachedTickets = monitorService.getBreachedTickets();

        // Then
        assertThat(breachedTickets).hasSize(1);
        assertThat(breachedTickets.get(0).getTicketNumber()).isEqualTo("TEST-1");
    }

    @Test
    @DisplayName("Should initialize SLA for new ticket")
    void shouldInitializeSlaForNewTicket() {
        // Given
        ticket.setCreatedAt(LocalDateTime.now());
        LocalDateTime expectedFirstResponseDue = LocalDateTime.now().plusMinutes(60);
        LocalDateTime expectedResolutionDue = LocalDateTime.now().plusMinutes(480);

        when(policyRepository.findByProjectId(project.getId())).thenReturn(List.of(slaPolicy));
        when(priorityRepository.findByPolicyIdAndPriority(slaPolicy.getId(), TicketPriority.MEDIUM))
                .thenReturn(Optional.of(slaPriority));
        when(calculatorService.calculateDueDate(any(), eq(60), any()))
                .thenReturn(expectedFirstResponseDue);
        when(calculatorService.calculateDueDate(any(), eq(480), any()))
                .thenReturn(expectedResolutionDue);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        monitorService.initializeSla(ticket);

        // Then
        assertThat(ticket.getSlaPolicy()).isEqualTo(slaPolicy);
        assertThat(ticket.getSlaFirstResponseDue()).isEqualTo(expectedFirstResponseDue);
        assertThat(ticket.getSlaResolutionDue()).isEqualTo(expectedResolutionDue);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should pause SLA when ticket goes to PENDING status")
    void shouldPauseSlaProperly() {
        // Given
        ticket.setSlaPolicy(slaPolicy);
        ticket.setSlaPausedAt(null);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        monitorService.pauseSla(ticket);

        // Then
        assertThat(ticket.getSlaPausedAt()).isNotNull();
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should resume SLA and extend due dates")
    void shouldResumeSlaProperly() {
        // Given
        LocalDateTime pausedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime originalFirstResponseDue = LocalDateTime.now().plusMinutes(30);
        LocalDateTime originalResolutionDue = LocalDateTime.now().plusHours(4);

        ticket.setSlaPolicy(slaPolicy);
        ticket.setSlaPausedAt(pausedAt);
        ticket.setSlaPausedMinutes(0);
        ticket.setSlaFirstResponseDue(originalFirstResponseDue);
        ticket.setSlaResolutionDue(originalResolutionDue);

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        monitorService.resumeSla(ticket);

        // Then
        assertThat(ticket.getSlaPausedAt()).isNull();
        assertThat(ticket.getSlaPausedMinutes()).isGreaterThanOrEqualTo(29);
        assertThat(ticket.getSlaFirstResponseDue()).isAfter(originalFirstResponseDue);
        assertThat(ticket.getSlaResolutionDue()).isAfter(originalResolutionDue);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should get SLA metrics correctly")
    void shouldGetSlaMetricsCorrectly() {
        // Given
        ticket.setSlaPolicy(slaPolicy);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusHours(1));
        ticket.setSlaResolutionDue(LocalDateTime.now().plusHours(8));
        ticket.setSlaFirstResponseBreached(false);
        ticket.setSlaResolutionBreached(false);

        Ticket resolvedTicket = Ticket.builder()
                .ticketNumber("TEST-2")
                .subject("Resolved Ticket")
                .status(TicketStatus.RESOLVED)
                .priority(TicketPriority.MEDIUM)
                .project(project)
                .build();
        resolvedTicket.setId(UUID.randomUUID());
        resolvedTicket.setSlaPolicy(slaPolicy);
        resolvedTicket.setCreatedAt(LocalDateTime.now().minusHours(2));
        resolvedTicket.setFirstResponseAt(LocalDateTime.now().minusHours(1).minusMinutes(30));
        resolvedTicket.setResolvedAt(LocalDateTime.now().minusMinutes(30));
        resolvedTicket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(30));
        resolvedTicket.setSlaResolutionDue(LocalDateTime.now().plusHours(6));
        resolvedTicket.setSlaFirstResponseBreached(false);
        resolvedTicket.setSlaResolutionBreached(false);

        // Mock the optimized repository methods
        when(ticketRepository.countTicketsWithSlaPolicy()).thenReturn(2L);
        when(ticketRepository.countTicketsWithFirstResponseSla()).thenReturn(2L);
        when(ticketRepository.countTicketsWithResolutionSla()).thenReturn(2L);
        when(ticketRepository.countFirstResponseCompliant()).thenReturn(2L);
        when(ticketRepository.countResolutionCompliant()).thenReturn(2L);
        when(ticketRepository.findTicketsWithSlaPolicy()).thenReturn(List.of(ticket, resolvedTicket));

        // When
        SlaMetricsResponse metrics = monitorService.getSlaMetrics();

        // Then
        assertThat(metrics.getTotalTicketsWithSla()).isEqualTo(2);
        assertThat(metrics.getFirstResponseComplianceRate()).isGreaterThan(0);
        assertThat(metrics.getResolutionComplianceRate()).isGreaterThan(0);
    }
}
