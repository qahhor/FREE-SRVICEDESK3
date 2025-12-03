package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NotificationPreference entity
 */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    /**
     * Find preferences by user ID
     */
    Optional<NotificationPreference> findByUserId(UUID userId);

    /**
     * Check if preferences exist for a user
     */
    boolean existsByUserId(UUID userId);
}
