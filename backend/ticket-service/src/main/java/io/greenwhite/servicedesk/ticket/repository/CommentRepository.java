package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Comment entity
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Find all comments for a ticket (excluding deleted)
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Find all comments for a ticket with pagination
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.deleted = false")
    Page<Comment> findByTicketId(@Param("ticketId") UUID ticketId, Pageable pageable);

    /**
     * Find only public comments for a ticket (for customers)
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.isInternal = false AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findPublicCommentsByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Count comments for a ticket
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.ticket.id = :ticketId AND c.deleted = false")
    Long countByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Find comments by author
     */
    @Query("SELECT c FROM Comment c WHERE c.author.id = :authorId AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorId(@Param("authorId") UUID authorId, Pageable pageable);
}
