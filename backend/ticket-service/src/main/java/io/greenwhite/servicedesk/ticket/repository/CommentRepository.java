package io.greenwhite.servicedesk.ticket.repository;

import io.greenwhite.servicedesk.ticket.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Comment entity
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
