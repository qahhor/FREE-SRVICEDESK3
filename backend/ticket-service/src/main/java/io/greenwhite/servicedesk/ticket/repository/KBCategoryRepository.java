package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.KBCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for KBCategory entity
 */
@Repository
public interface KBCategoryRepository extends JpaRepository<KBCategory, UUID> {

    Optional<KBCategory> findBySlugAndDeletedFalse(String slug);

    List<KBCategory> findByParentIsNullAndActiveTrueAndDeletedFalseOrderBySortOrderAsc();

    List<KBCategory> findByParentIdAndActiveTrueAndDeletedFalseOrderBySortOrderAsc(UUID parentId);

    List<KBCategory> findByActiveTrueAndDeletedFalseOrderBySortOrderAsc();

    @Query("SELECT c FROM KBCategory c WHERE c.active = true AND c.deleted = false ORDER BY c.sortOrder ASC")
    List<KBCategory> findAllActive();

    boolean existsBySlug(String slug);
}
