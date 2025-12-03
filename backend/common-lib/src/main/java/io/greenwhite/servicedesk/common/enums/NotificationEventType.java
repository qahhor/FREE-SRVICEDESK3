package io.greenwhite.servicedesk.common.enums;

/**
 * Event types for notification triggers
 */
public enum NotificationEventType {
    TICKET_CREATED,
    TICKET_ASSIGNED,
    TICKET_UNASSIGNED,
    TICKET_COMMENTED,
    TICKET_STATUS_CHANGED,
    TICKET_PRIORITY_CHANGED,
    TICKET_RESOLVED,
    TICKET_CLOSED,
    TICKET_REOPENED,
    SLA_WARNING,
    SLA_BREACHED,
    MENTION_IN_COMMENT,
    USER_REGISTERED,
    PASSWORD_RESET
}
