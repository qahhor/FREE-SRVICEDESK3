package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.ticket.model.SlaPriority;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
