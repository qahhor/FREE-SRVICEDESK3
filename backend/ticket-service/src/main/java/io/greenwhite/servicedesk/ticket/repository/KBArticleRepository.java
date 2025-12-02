package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.KBArticle;
import io.greenwhite.servicedesk.ticket.model.KBArticle.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for KBArticle entity
 */
@Repository
public interface KBArticleRepository extends JpaRepository<KBArticle, UUID> {

    Optional<KBArticle> findBySlugAndDeletedFalse(String slug);

    Page<KBArticle> findByStatusAndDeletedFalse(ArticleStatus status, Pageable pageable);

    Page<KBArticle> findByCategoryIdAndStatusAndDeletedFalse(UUID categoryId, ArticleStatus status, Pageable pageable);

    @Query("SELECT a FROM KBArticle a WHERE a.status = :status AND a.deleted = false ORDER BY a.viewCount DESC")
    List<KBArticle> findPopularArticles(@Param("status") ArticleStatus status, Pageable pageable);

    @Query("SELECT a FROM KBArticle a WHERE a.status = :status AND a.deleted = false ORDER BY a.publishedAt DESC")
    List<KBArticle> findRecentArticles(@Param("status") ArticleStatus status, Pageable pageable);

    @Query("SELECT a FROM KBArticle a WHERE a.status = :status AND a.deleted = false AND " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<KBArticle> searchArticles(@Param("query") String query, @Param("status") ArticleStatus status, Pageable pageable);

    @Query("SELECT COUNT(a) FROM KBArticle a WHERE a.category.id = :categoryId AND a.status = 'PUBLISHED' AND a.deleted = false")
    Long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Modifying
    @Query("UPDATE KBArticle a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE KBArticle a SET a.helpfulCount = a.helpfulCount + 1 WHERE a.id = :id")
    void incrementHelpfulCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE KBArticle a SET a.notHelpfulCount = a.notHelpfulCount + 1 WHERE a.id = :id")
    void incrementNotHelpfulCount(@Param("id") UUID id);

    @Query("SELECT a FROM KBArticle a WHERE a.category.id = :categoryId AND a.id != :articleId AND a.status = 'PUBLISHED' AND a.deleted = false ORDER BY a.viewCount DESC")
    List<KBArticle> findRelatedArticles(@Param("categoryId") UUID categoryId, @Param("articleId") UUID articleId, Pageable pageable);

    boolean existsBySlug(String slug);
}
