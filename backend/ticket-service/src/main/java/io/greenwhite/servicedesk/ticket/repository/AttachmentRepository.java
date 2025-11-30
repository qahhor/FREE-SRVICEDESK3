package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Attachment entity
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    /**
     * Find all attachments for a ticket
     */
    @Query("SELECT a FROM Attachment a WHERE a.ticket.id = :ticketId AND a.deleted = false ORDER BY a.createdAt ASC")
    List<Attachment> findByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Find all attachments for a comment
     */
    @Query("SELECT a FROM Attachment a WHERE a.comment.id = :commentId AND a.deleted = false ORDER BY a.createdAt ASC")
    List<Attachment> findByCommentId(@Param("commentId") UUID commentId);

    /**
     * Find all attachments uploaded by a user
     */
    @Query("SELECT a FROM Attachment a WHERE a.uploadedBy.id = :userId AND a.deleted = false ORDER BY a.createdAt DESC")
    List<Attachment> findByUploadedById(@Param("userId") UUID userId);

    /**
     * Count attachments for a ticket
     */
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.ticket.id = :ticketId AND a.deleted = false")
    Long countByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Count attachments for a comment
     */
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.comment.id = :commentId AND a.deleted = false")
    Long countByCommentId(@Param("commentId") UUID commentId);

    /**
     * Calculate total storage size for a user's uploads
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.uploadedBy.id = :userId AND a.deleted = false")
    Long calculateTotalSizeByUser(@Param("userId") UUID userId);
}
