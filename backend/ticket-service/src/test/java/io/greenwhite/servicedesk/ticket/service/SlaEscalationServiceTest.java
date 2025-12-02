package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.EscalationAction;
import io.greenwhite.servicedesk.common.enums.EscalationType;
import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.ticket.dto.SlaBreachAlert;
import io.greenwhite.servicedesk.ticket.model.*;
import io.greenwhite.servicedesk.ticket.repository.SlaEscalationRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SlaEscalationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SlaEscalationService Tests")
class SlaEscalationServiceTest {

    @Mock
    private SlaEscalationRepository escalationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SlaEscalationService escalationService;

    private SlaPolicy slaPolicy;
    private SlaEscalation escalation;
    private Ticket ticket;
    private User notifyUser;
    private User reassignUser;
    private Team team;

    @BeforeEach
    void setUp() {
        UUID policyId = UUID.randomUUID();

        slaPolicy = SlaPolicy.builder()
                .name("Standard SLA")
                .isDefault(true)
                .active(true)
                .build();
        slaPolicy.setId(policyId);

        notifyUser = User.builder()
                .email("notify@test.com")
                .firstName("Notify")
                .lastName("User")
                .build();
        notifyUser.setId(UUID.randomUUID());

        reassignUser = User.builder()
                .email("reassign@test.com")
                .firstName("Reassign")
                .lastName("User")
                .build();
        reassignUser.setId(UUID.randomUUID());

        User manager = User.builder()
                .email("manager@test.com")
                .firstName("Team")
                .lastName("Manager")
                .build();
        manager.setId(UUID.randomUUID());

        team = Team.builder()
                .name("Support Team")
                .manager(manager)
                .build();
        team.setId(UUID.randomUUID());

        escalation = SlaEscalation.builder()
                .policy(slaPolicy)
                .name("First Response Warning")
                .type(EscalationType.FIRST_RESPONSE)
                .triggerMinutesBefore(30)
                .action(EscalationAction.NOTIFY_EMAIL)
                .notifyUsers(Set.of(notifyUser))
                .active(true)
                .build();
        escalation.setId(UUID.randomUUID());

        Project project = Project.builder()
                .key("TEST")
                .name("Test Project")
                .build();
        project.setId(UUID.randomUUID());

        ticket = Ticket.builder()
                .ticketNumber("TEST-1")
                .subject("Test Ticket")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.MEDIUM)
                .project(project)
                .team(team)
                .build();
        ticket.setId(UUID.randomUUID());
        ticket.setSlaPolicy(slaPolicy);
        ticket.setCreatedAt(LocalDateTime.now().minusMinutes(40));
    }

    @Test
    @DisplayName("Should trigger first response escalation when approaching breach")
    void shouldTriggerFirstResponseEscalation() {
        // Given
        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(20)); // 20 mins until breach

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(escalation));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getBreachType()).isEqualTo(EscalationType.FIRST_RESPONSE);
        assertThat(alerts.get(0).getTicketNumber()).isEqualTo("TEST-1");
        assertThat(alerts.get(0).isBreached()).isFalse();
    }

    @Test
    @DisplayName("Should not trigger escalation when not approaching breach")
    void shouldNotTriggerEscalationWhenNotApproachingBreach() {
        // Given
        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(60)); // 60 mins until breach

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(escalation));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should not trigger escalation when already responded")
    void shouldNotTriggerEscalationWhenAlreadyResponded() {
        // Given
        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(20));
        ticket.setFirstResponseAt(LocalDateTime.now().minusMinutes(10)); // Already responded

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(escalation));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should mark alert as breached when past due time")
    void shouldMarkAlertAsBreachedWhenPastDueTime() {
        // Given
        ticket.setSlaFirstResponseDue(LocalDateTime.now().minusMinutes(10)); // Already past due

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(escalation));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).isBreached()).isTrue();
    }

    @Test
    @DisplayName("Should trigger resolution escalation")
    void shouldTriggerResolutionEscalation() {
        // Given
        SlaEscalation resolutionEscalation = SlaEscalation.builder()
                .policy(slaPolicy)
                .name("Resolution Warning")
                .type(EscalationType.RESOLUTION)
                .triggerMinutesBefore(60)
                .action(EscalationAction.NOTIFY_EMAIL)
                .notifyUsers(Set.of(notifyUser))
                .active(true)
                .build();
        resolutionEscalation.setId(UUID.randomUUID());

        ticket.setFirstResponseAt(LocalDateTime.now().minusMinutes(30)); // Already responded
        ticket.setSlaResolutionDue(LocalDateTime.now().plusMinutes(45)); // 45 mins until breach

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(resolutionEscalation));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getBreachType()).isEqualTo(EscalationType.RESOLUTION);
    }

    @Test
    @DisplayName("Should execute reassign ticket action")
    void shouldExecuteReassignTicketAction() {
        // Given
        SlaEscalation reassignEscalation = SlaEscalation.builder()
                .policy(slaPolicy)
                .name("Reassign on Warning")
                .type(EscalationType.FIRST_RESPONSE)
                .triggerMinutesBefore(30)
                .action(EscalationAction.REASSIGN_TICKET)
                .reassignTo(reassignUser)
                .active(true)
                .build();
        reassignEscalation.setId(UUID.randomUUID());

        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(20));

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(reassignEscalation));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(ticket.getAssignee()).isEqualTo(reassignUser);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should execute escalate to manager action")
    void shouldExecuteEscalateToManagerAction() {
        // Given
        SlaEscalation escalateEscalation = SlaEscalation.builder()
                .policy(slaPolicy)
                .name("Escalate to Manager")
                .type(EscalationType.FIRST_RESPONSE)
                .triggerMinutesBefore(30)
                .action(EscalationAction.ESCALATE_MANAGER)
                .active(true)
                .build();
        escalateEscalation.setId(UUID.randomUUID());

        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(20));

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(escalateEscalation));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(ticket.getAssignee()).isEqualTo(team.getManager());
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should execute increase priority action")
    void shouldExecuteIncreasePriorityAction() {
        // Given
        SlaEscalation priorityEscalation = SlaEscalation.builder()
                .policy(slaPolicy)
                .name("Increase Priority")
                .type(EscalationType.FIRST_RESPONSE)
                .triggerMinutesBefore(30)
                .action(EscalationAction.INCREASE_PRIORITY)
                .active(true)
                .build();
        priorityEscalation.setId(UUID.randomUUID());

        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(20));
        ticket.setPriority(TicketPriority.MEDIUM);

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(priorityEscalation));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should process escalations for multiple tickets")
    void shouldProcessEscalationsForMultipleTickets() {
        // Given
        Ticket ticket2 = Ticket.builder()
                .ticketNumber("TEST-2")
                .subject("Test Ticket 2")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.HIGH)
                .project(ticket.getProject())
                .build();
        ticket2.setId(UUID.randomUUID());
        ticket2.setSlaPolicy(slaPolicy);
        ticket2.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(15));

        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(20));

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(escalation));

        // When
        List<SlaBreachAlert> alerts = escalationService.processEscalations(List.of(ticket, ticket2));

        // Then
        assertThat(alerts).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when ticket has no SLA policy")
    void shouldReturnEmptyListWhenNoSlaPolicy() {
        // Given
        ticket.setSlaPolicy(null);

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).isEmpty();
        verify(escalationRepository, never()).findByPolicyIdAndActiveTrue(any());
    }

    @Test
    @DisplayName("Should not increase priority beyond CRITICAL")
    void shouldNotIncreasePriorityBeyondCritical() {
        // Given
        SlaEscalation priorityEscalation = SlaEscalation.builder()
                .policy(slaPolicy)
                .name("Increase Priority")
                .type(EscalationType.FIRST_RESPONSE)
                .triggerMinutesBefore(30)
                .action(EscalationAction.INCREASE_PRIORITY)
                .active(true)
                .build();
        priorityEscalation.setId(UUID.randomUUID());

        ticket.setSlaFirstResponseDue(LocalDateTime.now().plusMinutes(20));
        ticket.setPriority(TicketPriority.CRITICAL);

        when(escalationRepository.findByPolicyIdAndActiveTrue(slaPolicy.getId()))
                .thenReturn(List.of(priorityEscalation));

        // When
        List<SlaBreachAlert> alerts = escalationService.checkAndTriggerEscalations(ticket);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.CRITICAL);
        verify(ticketRepository, never()).save(ticket);
    }
}
