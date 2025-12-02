package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * SLA Priority entity for defining SLA targets per priority level
 */
@Entity
@Table(name = "sla_priority_targets", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "priority"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPriority extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private SlaPolicy policy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    /**
     * Target time in minutes for first response
     */
    @Column(name = "first_response_minutes")
    private Integer firstResponseMinutes;

    /**
     * Target time in minutes for resolution
     */
    @Column(name = "resolution_minutes")
    private Integer resolutionMinutes;

    /**
     * Target time in minutes for subsequent responses
     */
    @Column(name = "next_response_minutes")
    private Integer nextResponseMinutes;

    @Column(name = "first_response_enabled", nullable = false)
    @Builder.Default
    private Boolean firstResponseEnabled = true;

    @Column(name = "resolution_enabled", nullable = false)
    @Builder.Default
    private Boolean resolutionEnabled = true;
}
