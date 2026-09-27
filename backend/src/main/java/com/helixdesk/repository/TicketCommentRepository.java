package com.helixdesk.repository;

import com.helixdesk.entity.TicketComment;
import com.helixdesk.enums.CommentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    List<TicketComment> findByTicketIdAndTypeOrderByCreatedAtAsc(Long ticketId, CommentType type);
}
