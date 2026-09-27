package com.helixdesk.repository;

import com.helixdesk.entity.IntakeSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntakeSessionRepository extends JpaRepository<IntakeSession, Long> {
    Optional<IntakeSession> findByIdAndUserId(Long id, Long userId);
    List<IntakeSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}
