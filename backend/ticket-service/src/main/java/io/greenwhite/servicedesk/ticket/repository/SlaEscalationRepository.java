package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.common.enums.EscalationType;
import io.greenwhite.servicedesk.ticket.model.SlaEscalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for SlaEscalation entity
 */
@Repository
public interface SlaEscalationRepository extends JpaRepository<SlaEscalation, UUID> {

    List<SlaEscalation> findByPolicyId(UUID policyId);

    List<SlaEscalation> findByPolicyIdAndActiveTrue(UUID policyId);

    List<SlaEscalation> findByPolicyIdAndType(UUID policyId, EscalationType type);

    List<SlaEscalation> findByPolicyIdAndTypeAndActiveTrue(UUID policyId, EscalationType type);

    /**
     * Bulk delete escalations by policy ID
     */
    @Modifying
    @Query("DELETE FROM SlaEscalation se WHERE se.policy.id = :policyId")
    void deleteByPolicyId(@Param("policyId") UUID policyId);
}
