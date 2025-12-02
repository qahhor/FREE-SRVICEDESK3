package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SlaPolicy entity
 */
@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, UUID> {

    List<SlaPolicy> findByActiveTrue();

    Optional<SlaPolicy> findByIsDefaultTrue();

    @Query("SELECT sp FROM SlaPolicy sp JOIN sp.projects p WHERE p.id = :projectId AND sp.active = true")
    List<SlaPolicy> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT sp FROM SlaPolicy sp WHERE sp.name = :name")
    Optional<SlaPolicy> findByName(@Param("name") String name);
}
