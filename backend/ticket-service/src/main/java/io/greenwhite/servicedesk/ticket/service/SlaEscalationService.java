package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.EscalationAction;
import io.greenwhite.servicedesk.common.enums.EscalationType;
import io.greenwhite.servicedesk.ticket.dto.SlaBreachAlert;
import io.greenwhite.servicedesk.ticket.model.SlaEscalation;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.SlaEscalationRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling SLA escalations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlaEscalationService {

    private final SlaEscalationRepository escalationRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    /**
     * Check and trigger escalations for a ticket
     *
     * @param ticket The ticket to check
     * @return List of triggered escalation alerts
     */
    @Transactional
    public List<SlaBreachAlert> checkAndTriggerEscalations(Ticket ticket) {
        List<SlaBreachAlert> alerts = new ArrayList<>();

        if (ticket.getSlaPolicy() == null) {
            return alerts;
        }

        List<SlaEscalation> escalations = escalationRepository.findByPolicyIdAndActiveTrue(
                ticket.getSlaPolicy().getId());

        LocalDateTime now = LocalDateTime.now();

        for (SlaEscalation escalation : escalations) {
            SlaBreachAlert alert = checkEscalation(ticket, escalation, now);
            if (alert != null) {
                executeEscalationAction(ticket, escalation);
                alerts.add(alert);
            }
        }

        return alerts;
    }

    /**
     * Process escalations for tickets approaching breach
     *
     * @param tickets Tickets to process
     * @return List of all triggered alerts
     */
    @Transactional
    public List<SlaBreachAlert> processEscalations(List<Ticket> tickets) {
        List<SlaBreachAlert> allAlerts = new ArrayList<>();

        for (Ticket ticket : tickets) {
            List<SlaBreachAlert> ticketAlerts = checkAndTriggerEscalations(ticket);
            allAlerts.addAll(ticketAlerts);
        }

        return allAlerts;
    }

    private SlaBreachAlert checkEscalation(Ticket ticket, SlaEscalation escalation, LocalDateTime now) {
        LocalDateTime dueDate = null;
        boolean shouldTrigger = false;
        boolean breached = false;

        if (escalation.getType() == EscalationType.FIRST_RESPONSE) {
            // Check first response SLA
            if (ticket.getFirstResponseAt() != null || ticket.getSlaFirstResponseDue() == null) {
                return null; // Already responded or no SLA set
            }
            dueDate = ticket.getSlaFirstResponseDue();
        } else if (escalation.getType() == EscalationType.RESOLUTION) {
            // Check resolution SLA
            if (ticket.getResolvedAt() != null || ticket.getSlaResolutionDue() == null) {
                return null; // Already resolved or no SLA set
            }
            dueDate = ticket.getSlaResolutionDue();
        }

        if (dueDate == null) {
            return null;
        }

        int minutesUntilBreach = (int) Duration.between(now, dueDate).toMinutes();

        // Check if we should trigger this escalation
        if (minutesUntilBreach <= escalation.getTriggerMinutesBefore()) {
            shouldTrigger = true;
            breached = minutesUntilBreach <= 0;
        }

        if (!shouldTrigger) {
            return null;
        }

        return SlaBreachAlert.builder()
                .ticketId(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .breachType(escalation.getType())
                .dueAt(dueDate)
                .minutesUntilBreach(minutesUntilBreach)
                .breached(breached)
                .escalationId(escalation.getId())
                .escalationName(escalation.getName())
                .escalationAction(escalation.getAction())
                .alertTime(now)
                .build();
    }

    private void executeEscalationAction(Ticket ticket, SlaEscalation escalation) {
        EscalationAction action = escalation.getAction();

        switch (action) {
            case NOTIFY_EMAIL -> sendEmailNotification(ticket, escalation);
            case NOTIFY_SLACK -> sendSlackNotification(ticket, escalation);
            case REASSIGN_TICKET -> reassignTicket(ticket, escalation);
            case ESCALATE_MANAGER -> escalateToManager(ticket, escalation);
            case INCREASE_PRIORITY -> increasePriority(ticket);
        }

        log.info("Executed escalation action {} for ticket {}", action, ticket.getTicketNumber());
    }

    private void sendEmailNotification(Ticket ticket, SlaEscalation escalation) {
        // Log the notification (actual email sending would be implemented with email service)
        for (User user : escalation.getNotifyUsers()) {
            log.info("Sending SLA breach email notification to {} for ticket {}",
                    user.getEmail(), ticket.getTicketNumber());
        }
    }

    private void sendSlackNotification(Ticket ticket, SlaEscalation escalation) {
        // Log the notification (actual Slack integration would be implemented separately)
        for (User user : escalation.getNotifyUsers()) {
            log.info("Sending SLA breach Slack notification to {} for ticket {}",
                    user.getEmail(), ticket.getTicketNumber());
        }
    }

    private void reassignTicket(Ticket ticket, SlaEscalation escalation) {
        if (escalation.getReassignTo() != null) {
            ticket.setAssignee(escalation.getReassignTo());
            ticketRepository.save(ticket);
            log.info("Ticket {} reassigned to {} due to SLA escalation",
                    ticket.getTicketNumber(), escalation.getReassignTo().getEmail());
        }
    }

    private void escalateToManager(Ticket ticket, SlaEscalation escalation) {
        // Find team manager and reassign
        if (ticket.getTeam() != null && ticket.getTeam().getManager() != null) {
            ticket.setAssignee(ticket.getTeam().getManager());
            ticketRepository.save(ticket);
            log.info("Ticket {} escalated to team manager {} due to SLA breach",
                    ticket.getTicketNumber(), ticket.getTeam().getManager().getEmail());
        } else {
            log.warn("Cannot escalate ticket {} to manager - no team or manager assigned",
                    ticket.getTicketNumber());
        }
    }

    private void increasePriority(Ticket ticket) {
        switch (ticket.getPriority()) {
            case LOW -> ticket.setPriority(io.greenwhite.servicedesk.common.enums.TicketPriority.MEDIUM);
            case MEDIUM -> ticket.setPriority(io.greenwhite.servicedesk.common.enums.TicketPriority.HIGH);
            case HIGH -> ticket.setPriority(io.greenwhite.servicedesk.common.enums.TicketPriority.URGENT);
            case URGENT -> ticket.setPriority(io.greenwhite.servicedesk.common.enums.TicketPriority.CRITICAL);
            case CRITICAL -> {
                // Already at highest priority
                return;
            }
        }
        ticketRepository.save(ticket);
        log.info("Ticket {} priority increased to {} due to SLA escalation",
                ticket.getTicketNumber(), ticket.getPriority());
    }
}
