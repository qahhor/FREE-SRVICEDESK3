package io.greenwhite.servicedesk.common.enums;

/**
 * Escalation action enumeration for SLA escalation rules
 */
public enum EscalationAction {
    NOTIFY_EMAIL,       // Send email notification
    NOTIFY_SLACK,       // Send Slack notification
    REASSIGN_TICKET,    // Reassign to specific user
    ESCALATE_MANAGER,   // Escalate to team manager
    INCREASE_PRIORITY   // Bump ticket priority
}
