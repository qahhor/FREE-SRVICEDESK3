package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PushSubscription entity
 */
@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {

    /**
     * Find all subscriptions for a user
     */
    List<PushSubscription> findByUserId(UUID userId);

    /**
     * Find subscription by user and endpoint
     */
    Optional<PushSubscription> findByUserIdAndEndpoint(UUID userId, String endpoint);

    /**
     * Delete subscription by endpoint
     */
    @Modifying
    @Query("DELETE FROM PushSubscription p WHERE p.endpoint = :endpoint")
    int deleteByEndpoint(@Param("endpoint") String endpoint);

    /**
     * Delete all subscriptions for a user
     */
    @Modifying
    @Query("DELETE FROM PushSubscription p WHERE p.user.id = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);

    /**
     * Count subscriptions for a user
     */
    long countByUserId(UUID userId);
}
