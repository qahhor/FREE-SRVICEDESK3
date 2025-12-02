package io.greenwhite.servicedesk.common.enums;

/**
 * SLA status enumeration for tracking SLA compliance
 */
public enum SlaStatus {
    ON_TRACK,           // Within SLA
    WARNING,            // Approaching breach (e.g., 80% time used)
    BREACHED,           // SLA breached
    PAUSED,             // SLA paused (e.g., pending customer)
    NOT_APPLICABLE      // No SLA policy
}
