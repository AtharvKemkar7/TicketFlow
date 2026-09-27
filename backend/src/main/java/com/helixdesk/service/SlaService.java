package com.helixdesk.service;

import com.helixdesk.entity.SlaPolicy;
import com.helixdesk.entity.Ticket;
import com.helixdesk.enums.NotificationType;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.SlaState;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.SlaPolicyRepository;
import com.helixdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
public class SlaService {

    private static final EnumSet<TicketStatus> OPEN = EnumSet.of(
            TicketStatus.NEW, TicketStatus.AI_PROCESSING, TicketStatus.AI_ATTEMPTING_RESOLUTION,
            TicketStatus.HUMAN_REQUIRED, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS,
            TicketStatus.WAITING_FOR_USER, TicketStatus.REOPENED, TicketStatus.REASSIGNED, TicketStatus.ESCALATED,
            TicketStatus.RESOLVED, TicketStatus.USER_CONFIRMATION
    );

    private final SlaPolicyRepository slaPolicyRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public SlaService(
            SlaPolicyRepository slaPolicyRepository,
            TicketRepository ticketRepository,
            NotificationService notificationService,
            AuditService auditService
    ) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public void applySla(Ticket ticket) {
        SlaPolicy policy = slaPolicyRepository.findByPriority(ticket.getPriority())
                .orElseGet(() -> defaultPolicy(ticket.getPriority()));
        LocalDateTime now = LocalDateTime.now();
        ticket.setSlaResponseDueAt(now.plusMinutes(policy.getResponseTargetMinutes()));
        ticket.setSlaResolutionDueAt(now.plusMinutes(policy.getResolutionTargetMinutes()));
        ticket.setSlaState(SlaState.ON_TRACK);
    }

    public List<SlaPolicy> all() {
        return slaPolicyRepository.findAll();
    }

    @Transactional
    public SlaPolicy upsert(Priority priority, int response, int resolution, int atRisk) {
        SlaPolicy policy = slaPolicyRepository.findByPriority(priority).orElseGet(SlaPolicy::new);
        policy.setPriority(priority);
        policy.setResponseTargetMinutes(response);
        policy.setResolutionTargetMinutes(resolution);
        policy.setAtRiskThresholdMinutes(atRisk);
        return slaPolicyRepository.save(policy);
    }

    public SlaPolicy get(Long id) {
        return slaPolicyRepository.findById(id).orElseThrow(() -> new NotFoundException("SLA policy not found"));
    }

    @Transactional
    public void refreshOpenTickets() {
        LocalDateTime now = LocalDateTime.now();
        for (Ticket ticket : ticketRepository.findAll()) {
            if (!OPEN.contains(ticket.getStatus()) || ticket.getSlaResolutionDueAt() == null) {
                continue;
            }
            SlaPolicy policy = slaPolicyRepository.findByPriority(ticket.getPriority())
                    .orElseGet(() -> defaultPolicy(ticket.getPriority()));
            SlaState previous = ticket.getSlaState();
            if (now.isAfter(ticket.getSlaResolutionDueAt())) {
                ticket.setSlaState(SlaState.BREACHED);
                if (previous != SlaState.BREACHED) {
                    notificationService.notify(ticket.getRequester(), ticket, NotificationType.SLA_BREACHED,
                            "SLA breached for " + ticket.getTicketNumber());
                    auditService.record(null, ticket, com.helixdesk.enums.AuditAction.SLA_BREACHED,
                            previous.name(), SlaState.BREACHED.name(), null);
                }
            } else if (now.plusMinutes(policy.getAtRiskThresholdMinutes()).isAfter(ticket.getSlaResolutionDueAt())) {
                ticket.setSlaState(SlaState.AT_RISK);
                if (previous == SlaState.ON_TRACK) {
                    notificationService.notify(ticket.getRequester(), ticket, NotificationType.SLA_WARNING,
                            "SLA at risk for " + ticket.getTicketNumber());
                    auditService.record(null, ticket, com.helixdesk.enums.AuditAction.SLA_AT_RISK,
                            previous.name(), SlaState.AT_RISK.name(), null);
                }
            }
        }
    }

    private SlaPolicy defaultPolicy(Priority priority) {
        SlaPolicy policy = new SlaPolicy();
        policy.setPriority(priority);
        switch (priority) {
            case CRITICAL -> {
                policy.setResponseTargetMinutes(15);
                policy.setResolutionTargetMinutes(120);
                policy.setAtRiskThresholdMinutes(20);
            }
            case HIGH -> {
                policy.setResponseTargetMinutes(30);
                policy.setResolutionTargetMinutes(240);
                policy.setAtRiskThresholdMinutes(30);
            }
            case MEDIUM -> {
                policy.setResponseTargetMinutes(120);
                policy.setResolutionTargetMinutes(480);
                policy.setAtRiskThresholdMinutes(60);
            }
            default -> {
                policy.setResponseTargetMinutes(240);
                policy.setResolutionTargetMinutes(1440);
                policy.setAtRiskThresholdMinutes(120);
            }
        }
        return policy;
    }
}
