package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.ticket.model.SlaPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SlaPriority entity
 */
@Repository
public interface SlaPriorityRepository extends JpaRepository<SlaPriority, UUID> {

    List<SlaPriority> findByPolicyId(UUID policyId);

    Optional<SlaPriority> findByPolicyIdAndPriority(UUID policyId, TicketPriority priority);

    /**
     * Bulk delete priorities by policy ID
     */
    @Modifying
    @Query("DELETE FROM SlaPriority sp WHERE sp.policy.id = :policyId")
    void deleteByPolicyId(@Param("policyId") UUID policyId);
}
