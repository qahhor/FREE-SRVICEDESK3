package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.SlaStatus;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.ticket.dto.SlaMetricsResponse;
import io.greenwhite.servicedesk.ticket.model.SlaPolicy;
import io.greenwhite.servicedesk.ticket.model.SlaPriority;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.repository.SlaPolicyRepository;
import io.greenwhite.servicedesk.ticket.repository.SlaPriorityRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for monitoring SLA status and detecting breaches
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlaMonitorService {

    private final TicketRepository ticketRepository;
    private final SlaPolicyRepository policyRepository;
    private final SlaPriorityRepository priorityRepository;
    private final SlaCalculatorService calculatorService;

    private static final Set<TicketStatus> ACTIVE_STATUSES = Set.of(
            TicketStatus.NEW, TicketStatus.OPEN, TicketStatus.IN_PROGRESS,
            TicketStatus.PENDING, TicketStatus.REOPENED
    );

    private static final Set<TicketStatus> PAUSED_STATUSES = Set.of(
            TicketStatus.PENDING, TicketStatus.ON_HOLD
    );

    /**
     * Check SLA status for a ticket
     *
     * @param ticket The ticket to check
     * @return The SLA status
     */
    public SlaStatus checkTicketSla(Ticket ticket) {
        if (ticket.getSlaPolicy() == null) {
            return SlaStatus.NOT_APPLICABLE;
        }

        // Check if SLA is paused
        if (PAUSED_STATUSES.contains(ticket.getStatus()) && ticket.getSlaPausedAt() != null) {
            return SlaStatus.PAUSED;
        }

        LocalDateTime now = LocalDateTime.now();

        // Check first response SLA
        if (ticket.getFirstResponseAt() == null && ticket.getSlaFirstResponseDue() != null) {
            if (ticket.getSlaFirstResponseBreached() || now.isAfter(ticket.getSlaFirstResponseDue())) {
                return SlaStatus.BREACHED;
            }
            // Check warning threshold (80% of time used)
            if (isApproachingBreach(now, ticket.getCreatedAt(), ticket.getSlaFirstResponseDue(), 0.8)) {
                return SlaStatus.WARNING;
            }
        }

        // Check resolution SLA
        if (!isResolved(ticket.getStatus()) && ticket.getSlaResolutionDue() != null) {
            if (ticket.getSlaResolutionBreached() || now.isAfter(ticket.getSlaResolutionDue())) {
                return SlaStatus.BREACHED;
            }
            // Check warning threshold (80% of time used)
            if (isApproachingBreach(now, ticket.getCreatedAt(), ticket.getSlaResolutionDue(), 0.8)) {
                return SlaStatus.WARNING;
            }
        }

        return SlaStatus.ON_TRACK;
    }

    /**
     * Get tickets approaching SLA breach
     *
     * @param warningMinutes Minutes before breach to consider as warning
     * @return List of tickets approaching breach
     */
    public List<Ticket> getTicketsApproachingBreach(int warningMinutes) {
        List<Ticket> allTickets = ticketRepository.findAll();
        List<Ticket> approachingBreach = new ArrayList<>();
        LocalDateTime warningThreshold = LocalDateTime.now().plusMinutes(warningMinutes);

        for (Ticket ticket : allTickets) {
            if (!ACTIVE_STATUSES.contains(ticket.getStatus())) {
                continue;
            }

            // Check first response
            if (ticket.getFirstResponseAt() == null &&
                    ticket.getSlaFirstResponseDue() != null &&
                    !ticket.getSlaFirstResponseBreached() &&
                    ticket.getSlaFirstResponseDue().isBefore(warningThreshold)) {
                approachingBreach.add(ticket);
                continue;
            }

            // Check resolution
            if (ticket.getSlaResolutionDue() != null &&
                    !ticket.getSlaResolutionBreached() &&
                    ticket.getSlaResolutionDue().isBefore(warningThreshold)) {
                approachingBreach.add(ticket);
            }
        }

        return approachingBreach;
    }

    /**
     * Get already breached tickets
     *
     * @return List of breached tickets
     */
    public List<Ticket> getBreachedTickets() {
        List<Ticket> allTickets = ticketRepository.findAll();
        List<Ticket> breached = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Ticket ticket : allTickets) {
            if (!ACTIVE_STATUSES.contains(ticket.getStatus())) {
                continue;
            }

            boolean isBreached = ticket.getSlaFirstResponseBreached() ||
                    ticket.getSlaResolutionBreached() ||
                    (ticket.getFirstResponseAt() == null &&
                            ticket.getSlaFirstResponseDue() != null &&
                            now.isAfter(ticket.getSlaFirstResponseDue())) ||
                    (ticket.getSlaResolutionDue() != null &&
                            now.isAfter(ticket.getSlaResolutionDue()));

            if (isBreached) {
                breached.add(ticket);
            }
        }

        return breached;
    }

    /**
     * Update SLA metrics when ticket is updated
     *
     * @param ticket The ticket being updated
     */
    @Transactional
    public void updateSlaMetrics(Ticket ticket) {
        if (ticket.getSlaPolicy() == null) {
            // Try to assign default SLA policy
            assignSlaPolicy(ticket);
        }

        if (ticket.getSlaPolicy() == null) {
            return; // No SLA policy, nothing to track
        }

        LocalDateTime now = LocalDateTime.now();

        // Mark first response breach if applicable
        if (ticket.getFirstResponseAt() == null &&
                ticket.getSlaFirstResponseDue() != null &&
                now.isAfter(ticket.getSlaFirstResponseDue())) {
            ticket.setSlaFirstResponseBreached(true);
        }

        // Mark resolution breach if applicable
        if (!isResolved(ticket.getStatus()) &&
                ticket.getSlaResolutionDue() != null &&
                now.isAfter(ticket.getSlaResolutionDue())) {
            ticket.setSlaResolutionBreached(true);
        }

        ticketRepository.save(ticket);
    }

    /**
     * Initialize SLA tracking for a new ticket
     *
     * @param ticket The new ticket
     */
    @Transactional
    public void initializeSla(Ticket ticket) {
        assignSlaPolicy(ticket);

        if (ticket.getSlaPolicy() != null) {
            calculateSlaDates(ticket);
            ticketRepository.save(ticket);
            log.info("SLA initialized for ticket {}: firstResponseDue={}, resolutionDue={}",
                    ticket.getTicketNumber(),
                    ticket.getSlaFirstResponseDue(),
                    ticket.getSlaResolutionDue());
        }
    }

    /**
     * Handle SLA pause when ticket status changes to PENDING or ON_HOLD
     *
     * @param ticket The ticket
     */
    @Transactional
    public void pauseSla(Ticket ticket) {
        if (ticket.getSlaPolicy() != null && ticket.getSlaPausedAt() == null) {
            ticket.setSlaPausedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
            log.info("SLA paused for ticket {}", ticket.getTicketNumber());
        }
    }

    /**
     * Handle SLA resume when ticket status changes from PENDING or ON_HOLD
     *
     * @param ticket The ticket
     */
    @Transactional
    public void resumeSla(Ticket ticket) {
        if (ticket.getSlaPolicy() != null && ticket.getSlaPausedAt() != null) {
            int pausedMinutes = (int) Duration.between(ticket.getSlaPausedAt(), LocalDateTime.now()).toMinutes();
            int totalPausedMinutes = (ticket.getSlaPausedMinutes() != null ? ticket.getSlaPausedMinutes() : 0) + pausedMinutes;
            ticket.setSlaPausedMinutes(totalPausedMinutes);
            ticket.setSlaPausedAt(null);

            // Extend SLA due dates by paused time
            if (ticket.getSlaFirstResponseDue() != null && ticket.getFirstResponseAt() == null) {
                ticket.setSlaFirstResponseDue(ticket.getSlaFirstResponseDue().plusMinutes(pausedMinutes));
            }
            if (ticket.getSlaResolutionDue() != null) {
                ticket.setSlaResolutionDue(ticket.getSlaResolutionDue().plusMinutes(pausedMinutes));
            }

            ticketRepository.save(ticket);
            log.info("SLA resumed for ticket {}. Total paused minutes: {}",
                    ticket.getTicketNumber(), totalPausedMinutes);
        }
    }

    /**
     * Get SLA metrics for dashboard
     *
     * @return SLA metrics
     */
    public SlaMetricsResponse getSlaMetrics() {
        List<Ticket> allTickets = ticketRepository.findAll();
        
        long totalWithSla = 0;
        long onTrack = 0;
        long warning = 0;
        long breached = 0;
        long firstResponseCompliant = 0;
        long firstResponseTotal = 0;
        long resolutionCompliant = 0;
        long resolutionTotal = 0;
        long totalFirstResponseMinutes = 0;
        long firstResponseCount = 0;
        long totalResolutionMinutes = 0;
        long resolutionCount = 0;

        for (Ticket ticket : allTickets) {
            if (ticket.getSlaPolicy() == null) {
                continue;
            }
            totalWithSla++;

            SlaStatus status = checkTicketSla(ticket);
            switch (status) {
                case ON_TRACK -> onTrack++;
                case WARNING -> warning++;
                case BREACHED -> breached++;
            }

            // First response compliance
            if (ticket.getSlaFirstResponseDue() != null) {
                firstResponseTotal++;
                if (!ticket.getSlaFirstResponseBreached() &&
                        (ticket.getFirstResponseAt() == null ||
                                !ticket.getFirstResponseAt().isAfter(ticket.getSlaFirstResponseDue()))) {
                    firstResponseCompliant++;
                }
                if (ticket.getFirstResponseAt() != null) {
                    totalFirstResponseMinutes += Duration.between(
                            ticket.getCreatedAt(), ticket.getFirstResponseAt()).toMinutes();
                    firstResponseCount++;
                }
            }

            // Resolution compliance
            if (ticket.getSlaResolutionDue() != null) {
                resolutionTotal++;
                if (!ticket.getSlaResolutionBreached() &&
                        (ticket.getResolvedAt() == null ||
                                !ticket.getResolvedAt().isAfter(ticket.getSlaResolutionDue()))) {
                    resolutionCompliant++;
                }
                if (ticket.getResolvedAt() != null) {
                    totalResolutionMinutes += Duration.between(
                            ticket.getCreatedAt(), ticket.getResolvedAt()).toMinutes();
                    resolutionCount++;
                }
            }
        }

        double firstResponseRate = firstResponseTotal > 0 ?
                (double) firstResponseCompliant / firstResponseTotal * 100 : 100;
        double resolutionRate = resolutionTotal > 0 ?
                (double) resolutionCompliant / resolutionTotal * 100 : 100;
        double overallRate = (firstResponseRate + resolutionRate) / 2;

        return SlaMetricsResponse.builder()
                .totalTicketsWithSla(totalWithSla)
                .ticketsOnTrack(onTrack)
                .ticketsInWarning(warning)
                .ticketsBreached(breached)
                .firstResponseComplianceRate(firstResponseRate)
                .resolutionComplianceRate(resolutionRate)
                .overallComplianceRate(overallRate)
                .averageFirstResponseMinutes(firstResponseCount > 0 ?
                        totalFirstResponseMinutes / firstResponseCount : 0)
                .averageResolutionMinutes(resolutionCount > 0 ?
                        totalResolutionMinutes / resolutionCount : 0)
                .build();
    }

    private void assignSlaPolicy(Ticket ticket) {
        // First try to find a policy for the project
        List<SlaPolicy> projectPolicies = policyRepository.findByProjectId(ticket.getProject().getId());
        if (!projectPolicies.isEmpty()) {
            ticket.setSlaPolicy(projectPolicies.get(0));
            return;
        }

        // Fall back to default policy
        Optional<SlaPolicy> defaultPolicy = policyRepository.findByIsDefaultTrue();
        defaultPolicy.ifPresent(ticket::setSlaPolicy);
    }

    private void calculateSlaDates(Ticket ticket) {
        SlaPolicy policy = ticket.getSlaPolicy();
        if (policy == null) {
            return;
        }

        Optional<SlaPriority> slaPriority = priorityRepository.findByPolicyIdAndPriority(
                policy.getId(), ticket.getPriority());

        if (slaPriority.isEmpty()) {
            return;
        }

        SlaPriority priority = slaPriority.get();
        LocalDateTime createdAt = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : LocalDateTime.now();

        // Calculate first response due date
        if (priority.getFirstResponseEnabled() && priority.getFirstResponseMinutes() != null) {
            LocalDateTime firstResponseDue = calculatorService.calculateDueDate(
                    createdAt,
                    priority.getFirstResponseMinutes(),
                    policy.getBusinessHours());
            ticket.setSlaFirstResponseDue(firstResponseDue);
        }

        // Calculate resolution due date
        if (priority.getResolutionEnabled() && priority.getResolutionMinutes() != null) {
            LocalDateTime resolutionDue = calculatorService.calculateDueDate(
                    createdAt,
                    priority.getResolutionMinutes(),
                    policy.getBusinessHours());
            ticket.setSlaResolutionDue(resolutionDue);
        }
    }

    private boolean isApproachingBreach(LocalDateTime now, LocalDateTime start, LocalDateTime due, double threshold) {
        if (start == null || due == null) {
            return false;
        }
        long totalMinutes = Duration.between(start, due).toMinutes();
        long elapsedMinutes = Duration.between(start, now).toMinutes();
        return elapsedMinutes >= totalMinutes * threshold;
    }

    private boolean isResolved(TicketStatus status) {
        return status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED;
    }
}
