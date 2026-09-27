package com.helixdesk.repository;

import com.helixdesk.entity.AiResolutionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiResolutionAttemptRepository extends JpaRepository<AiResolutionAttempt, Long> {
    List<AiResolutionAttempt> findByTicketIdOrderByAttemptNumberAsc(Long ticketId);
    long countByHandedOffTrue();
}
