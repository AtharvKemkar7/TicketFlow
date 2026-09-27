package com.helixdesk.service;

import com.helixdesk.entity.AuditLog;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AuditAction;
import com.helixdesk.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(UserAccount actor, Ticket ticket, AuditAction action, String previousValue, String newValue, String reason) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setActorName(actor != null ? actor.fullName() : "SYSTEM");
        log.setTicket(ticket);
        log.setAction(action);
        log.setPreviousValue(previousValue);
        log.setNewValue(newValue);
        log.setReason(reason);
        auditLogRepository.save(log);
    }

    public List<AuditLog> all() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AuditLog> forTicket(Long ticketId) {
        return auditLogRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }
}
