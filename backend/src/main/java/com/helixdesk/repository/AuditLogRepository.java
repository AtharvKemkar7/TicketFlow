package com.helixdesk.repository;

import com.helixdesk.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
