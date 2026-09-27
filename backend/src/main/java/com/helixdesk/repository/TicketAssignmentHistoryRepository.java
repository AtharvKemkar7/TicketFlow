package com.helixdesk.repository;

import com.helixdesk.entity.TicketAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAssignmentHistoryRepository extends JpaRepository<TicketAssignmentHistory, Long> {
    List<TicketAssignmentHistory> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
