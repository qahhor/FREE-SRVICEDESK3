package io.greenwhite.servicedesk.ticket.scheduler;

import io.greenwhite.servicedesk.ticket.dto.SlaBreachAlert;
import io.greenwhite.servicedesk.ticket.dto.SlaMetricsResponse;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.service.SlaEscalationService;
import io.greenwhite.servicedesk.ticket.service.SlaMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job for monitoring SLA breaches and triggering escalations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlaMonitorJob {

    private final SlaMonitorService monitorService;
    private final SlaEscalationService escalationService;

    /**
     * Check for SLA breaches every minute
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkSlaBreaches() {
        log.debug("Running SLA breach check...");

        try {
            // Get tickets approaching breach (within 60 minutes)
            List<Ticket> approachingBreach = monitorService.getTicketsApproachingBreach(60);
            
            if (!approachingBreach.isEmpty()) {
                log.info("Found {} tickets approaching SLA breach", approachingBreach.size());
                
                // Process escalations
                List<SlaBreachAlert> alerts = escalationService.processEscalations(approachingBreach);
                
                if (!alerts.isEmpty()) {
                    log.info("Triggered {} escalation alerts", alerts.size());
                    for (SlaBreachAlert alert : alerts) {
                        log.info("SLA Alert: Ticket {} - {} - {} minutes until {}",
                                alert.getTicketNumber(),
                                alert.isBreached() ? "BREACHED" : "WARNING",
                                alert.getMinutesUntilBreach(),
                                alert.getBreachType());
                    }
                }
            }

            // Check and mark breached tickets
            List<Ticket> breachedTickets = monitorService.getBreachedTickets();
            for (Ticket ticket : breachedTickets) {
                monitorService.updateSlaMetrics(ticket);
            }

        } catch (Exception e) {
            log.error("Error during SLA breach check: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate hourly SLA compliance report
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void generateSlaReport() {
        log.info("Generating hourly SLA compliance report...");

        try {
            SlaMetricsResponse metrics = monitorService.getSlaMetrics();

            log.info("=== SLA Compliance Report ===");
            log.info("Total tickets with SLA: {}", metrics.getTotalTicketsWithSla());
            log.info("Tickets on track: {}", metrics.getTicketsOnTrack());
            log.info("Tickets in warning: {}", metrics.getTicketsInWarning());
            log.info("Tickets breached: {}", metrics.getTicketsBreached());
            log.info("First Response Compliance: {:.2f}%", metrics.getFirstResponseComplianceRate());
            log.info("Resolution Compliance: {:.2f}%", metrics.getResolutionComplianceRate());
            log.info("Overall Compliance: {:.2f}%", metrics.getOverallComplianceRate());
            log.info("Average First Response: {} minutes", metrics.getAverageFirstResponseMinutes());
            log.info("Average Resolution: {} minutes", metrics.getAverageResolutionMinutes());
            log.info("=============================");

        } catch (Exception e) {
            log.error("Error generating SLA report: {}", e.getMessage(), e);
        }
    }
}
