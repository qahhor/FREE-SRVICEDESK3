package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.enums.EscalationAction;
import io.greenwhite.servicedesk.common.enums.EscalationType;
import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * SLA Escalation entity for defining escalation rules
 */
@Entity
@Table(name = "sla_escalations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaEscalation extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private SlaPolicy policy;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "escalation_type", nullable = false)
    private EscalationType type;

    /**
     * Minutes before breach to trigger escalation
     */
    @Column(name = "trigger_minutes_before", nullable = false)
    private Integer triggerMinutesBefore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscalationAction action;

    /**
     * Users to notify when escalation is triggered
     */
    @ManyToMany
    @JoinTable(
            name = "sla_escalation_notify_users",
            joinColumns = @JoinColumn(name = "escalation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> notifyUsers = new HashSet<>();

    /**
     * User to reassign ticket to when escalation action is REASSIGN_TICKET
     */
    @ManyToOne
    @JoinColumn(name = "reassign_to_user_id")
    private User reassignTo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
