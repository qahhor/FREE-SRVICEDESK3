package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Comment entity for ticket communication
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_ticket", columnList = "ticket_id"),
        @Index(name = "idx_comment_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(nullable = false)
    private Boolean isInternal = false;

    @Column(nullable = false)
    private Boolean isAutomated = false;

    @Column
    private String attachments;
}
