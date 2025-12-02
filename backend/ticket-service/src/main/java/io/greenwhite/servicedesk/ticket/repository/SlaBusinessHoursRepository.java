package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.SlaBusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for SlaBusinessHours entity
 */
@Repository
public interface SlaBusinessHoursRepository extends JpaRepository<SlaBusinessHours, UUID> {
}
