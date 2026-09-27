package com.helixdesk.repository;

import com.helixdesk.entity.TicketEscalationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketEscalationHistoryRepository extends JpaRepository<TicketEscalationHistory, Long> {
    List<TicketEscalationHistory> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
