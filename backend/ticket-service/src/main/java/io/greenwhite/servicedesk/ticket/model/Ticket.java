package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.enums.ChannelType;
import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket entity - main entity for service desk
 */
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_number", columnList = "ticketNumber"),
        @Index(name = "idx_ticket_status", columnList = "status"),
        @Index(name = "idx_ticket_priority", columnList = "priority"),
        @Index(name = "idx_ticket_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @Column(nullable = false)
    private String subject;

    @Column(length = 10000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channel;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column
    private String category;

    @Column
    private String tags;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @Column
    private LocalDateTime firstResponseAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column
    private LocalDateTime closedAt;

    @Column
    private LocalDateTime dueDate;

    @Column
    private Integer slaBreachMinutes;

    @Column(nullable = false)
    private Boolean isPublic = true;

    /**
     * Generate ticket number in format: PROJECT-NUMBER
     */
    public void generateTicketNumber(Long sequence) {
        this.ticketNumber = String.format("%s-%d", project.getKey(), sequence);
    }

    /**
     * Mark ticket as resolved
     */
    public void resolve() {
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Mark ticket as closed
     */
    public void close() {
        this.status = TicketStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * Reopen ticket
     */
    public void reopen() {
        this.status = TicketStatus.REOPENED;
        this.resolvedAt = null;
        this.closedAt = null;
    }

    /**
     * Assign ticket to user
     */
    public void assignTo(User user) {
        this.assignee = user;
        if (this.status == TicketStatus.NEW) {
            this.status = TicketStatus.OPEN;
        }
    }

    /**
     * Record first response time
     */
    public void recordFirstResponse() {
        if (this.firstResponseAt == null) {
            this.firstResponseAt = LocalDateTime.now();
        }
    }
}
