package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Ticket entity
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    Page<Ticket> findByAssigneeId(UUID assigneeId, Pageable pageable);

    Page<Ticket> findByRequesterId(UUID requesterId, Pageable pageable);

    Page<Ticket> findByProjectId(UUID projectId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
            "(:projectId IS NULL OR t.project.id = :projectId)")
    Page<Ticket> findByFilters(
            @Param("status") TicketStatus status,
            @Param("assigneeId") UUID assigneeId,
            @Param("projectId") UUID projectId,
            Pageable pageable
    );

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    long countByStatus(@Param("status") TicketStatus status);
}
