package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    // SLA-related queries

    /**
     * Find tickets with SLA policy that are approaching breach
     */
    @Query("SELECT t FROM Ticket t WHERE t.slaPolicy IS NOT NULL " +
            "AND t.status IN :activeStatuses " +
            "AND ((t.firstResponseAt IS NULL AND t.slaFirstResponseDue IS NOT NULL " +
            "      AND t.slaFirstResponseBreached = false AND t.slaFirstResponseDue < :warningThreshold) " +
            "  OR (t.slaResolutionDue IS NOT NULL AND t.slaResolutionBreached = false " +
            "      AND t.slaResolutionDue < :warningThreshold))")
    List<Ticket> findTicketsApproachingSla(
            @Param("activeStatuses") List<TicketStatus> activeStatuses,
            @Param("warningThreshold") LocalDateTime warningThreshold);

    /**
     * Find tickets with SLA breaches
     */
    @Query("SELECT t FROM Ticket t WHERE t.slaPolicy IS NOT NULL " +
            "AND t.status IN :activeStatuses " +
            "AND (t.slaFirstResponseBreached = true " +
            "  OR t.slaResolutionBreached = true " +
            "  OR (t.firstResponseAt IS NULL AND t.slaFirstResponseDue IS NOT NULL " +
            "      AND t.slaFirstResponseDue < :now) " +
            "  OR (t.slaResolutionDue IS NOT NULL AND t.slaResolutionDue < :now))")
    List<Ticket> findBreachedTickets(
            @Param("activeStatuses") List<TicketStatus> activeStatuses,
            @Param("now") LocalDateTime now);

    /**
     * Find all tickets with SLA policy
     */
    @Query("SELECT t FROM Ticket t WHERE t.slaPolicy IS NOT NULL")
    List<Ticket> findTicketsWithSlaPolicy();

    /**
     * Count tickets with SLA policy
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaPolicy IS NOT NULL")
    long countTicketsWithSlaPolicy();

    /**
     * Count tickets that met first response SLA
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaPolicy IS NOT NULL " +
            "AND t.slaFirstResponseDue IS NOT NULL " +
            "AND t.slaFirstResponseBreached = false " +
            "AND (t.firstResponseAt IS NULL OR t.firstResponseAt <= t.slaFirstResponseDue)")
    long countFirstResponseCompliant();

    /**
     * Count tickets that met resolution SLA
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaPolicy IS NOT NULL " +
            "AND t.slaResolutionDue IS NOT NULL " +
            "AND t.slaResolutionBreached = false " +
            "AND (t.resolvedAt IS NULL OR t.resolvedAt <= t.slaResolutionDue)")
    long countResolutionCompliant();

    /**
     * Count tickets with first response SLA
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaPolicy IS NOT NULL AND t.slaFirstResponseDue IS NOT NULL")
    long countTicketsWithFirstResponseSla();

    /**
     * Count tickets with resolution SLA
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaPolicy IS NOT NULL AND t.slaResolutionDue IS NOT NULL")
    long countTicketsWithResolutionSla();
}
