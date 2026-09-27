package com.helixdesk.service;

import com.helixdesk.entity.SpecialistProfile;
import com.helixdesk.entity.SpecialistSkill;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.TicketAssignmentHistory;
import com.helixdesk.entity.TicketEscalationHistory;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AccountStatus;
import com.helixdesk.enums.AuditAction;
import com.helixdesk.enums.AvailabilityStatus;
import com.helixdesk.enums.NotificationType;
import com.helixdesk.enums.SupportLevel;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.exception.BadRequestException;
import com.helixdesk.exception.ConflictException;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.SpecialistProfileRepository;
import com.helixdesk.repository.SpecialistSkillRepository;
import com.helixdesk.repository.TicketAssignmentHistoryRepository;
import com.helixdesk.repository.TicketEscalationHistoryRepository;
import com.helixdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentService {

    private final TicketRepository ticketRepository;
    private final SpecialistProfileRepository specialistProfileRepository;
    private final SpecialistSkillRepository specialistSkillRepository;
    private final TicketAssignmentHistoryRepository assignmentHistoryRepository;
    private final TicketEscalationHistoryRepository escalationHistoryRepository;
    private final TicketWorkflowService workflowService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public AssignmentService(
            TicketRepository ticketRepository,
            SpecialistProfileRepository specialistProfileRepository,
            SpecialistSkillRepository specialistSkillRepository,
            TicketAssignmentHistoryRepository assignmentHistoryRepository,
            TicketEscalationHistoryRepository escalationHistoryRepository,
            TicketWorkflowService workflowService,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.ticketRepository = ticketRepository;
        this.specialistProfileRepository = specialistProfileRepository;
        this.specialistSkillRepository = specialistSkillRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.escalationHistoryRepository = escalationHistoryRepository;
        this.workflowService = workflowService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Ticket assign(Long ticketId, Long specialistId, UserAccount actor, String reason) {
        Ticket ticket = lock(ticketId);
        if (ticket.getAssignedSpecialist() != null
                && List.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.WAITING_FOR_USER).contains(ticket.getStatus())) {
            throw new ConflictException("Ticket is already assigned");
        }
        SpecialistProfile specialist = validateSpecialist(ticket, specialistId, false);
        SpecialistProfile previous = ticket.getAssignedSpecialist();
        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            workflowService.assertTransition(ticket.getStatus(), TicketStatus.ASSIGNED);
        }
        applyAssignment(ticket, specialist, previous, actor, reason, "ASSIGN");
        ticket.setStatus(TicketStatus.ASSIGNED);
        return ticket;
    }

    @Transactional
    public Ticket reassign(Long ticketId, Long specialistId, UserAccount actor, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Reassignment reason is required");
        }
        Ticket ticket = lock(ticketId);
        SpecialistProfile current = ticket.getAssignedSpecialist();
        SpecialistProfile specialist = validateSpecialist(ticket, specialistId, false);
        if (current != null && current.getSupportLevel() != specialist.getSupportLevel()) {
            throw new BadRequestException("Reassignment must stay at the same support level");
        }
        if (current != null && current.getId().equals(specialist.getId())) {
            throw new BadRequestException("Ticket is already assigned to this specialist");
        }
        workflowService.assertTransition(ticket.getStatus(), TicketStatus.REASSIGNED);
        ticket.setStatus(TicketStatus.REASSIGNED);
        applyAssignment(ticket, specialist, current, actor, reason, "REASSIGN");
        ticket.setStatus(TicketStatus.ASSIGNED);
        auditService.record(actor, ticket, AuditAction.REASSIGNMENT,
                current != null ? current.getUser().fullName() : null,
                specialist.getUser().fullName(), reason);
        notificationService.notify(ticket.getRequester(), ticket, NotificationType.TICKET_REASSIGNED,
                "Ticket " + ticket.getTicketNumber() + " reassigned");
        return ticket;
    }

    @Transactional
    public Ticket escalate(Long ticketId, Long specialistId, UserAccount actor, String reason, String notes) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Escalation reason is required");
        }
        Ticket ticket = lock(ticketId);
        SpecialistProfile current = ticket.getAssignedSpecialist();
        SpecialistProfile specialist = validateSpecialist(ticket, specialistId, true);
        SupportLevel from = ticket.getCurrentSupportLevel() != null
                ? ticket.getCurrentSupportLevel()
                : (current != null ? current.getSupportLevel() : SupportLevel.L1);
        if (from == SupportLevel.L3) {
            throw new BadRequestException("Ticket is already at the highest support level");
        }
        if (!isHigher(from, specialist.getSupportLevel())) {
            throw new BadRequestException("Escalation must go to a higher support level");
        }
        workflowService.assertTransition(ticket.getStatus(), TicketStatus.ESCALATED);
        ticket.setStatus(TicketStatus.ESCALATED);
        TicketEscalationHistory history = new TicketEscalationHistory();
        history.setTicket(ticket);
        history.setFromLevel(from);
        history.setToLevel(specialist.getSupportLevel());
        history.setFromSpecialist(current);
        history.setToSpecialist(specialist);
        history.setReason(reason);
        history.setNotes(notes);
        escalationHistoryRepository.save(history);
        applyAssignment(ticket, specialist, current, actor, reason, "ESCALATE");
        ticket.setStatus(TicketStatus.ASSIGNED);
        auditService.record(actor, ticket, AuditAction.ESCALATION, from.name(), specialist.getSupportLevel().name(), reason);
        notificationService.notify(ticket.getRequester(), ticket, NotificationType.TICKET_ESCALATED,
                "Ticket " + ticket.getTicketNumber() + " escalated to " + specialist.getSupportLevel());
        return ticket;
    }

    public List<SpecialistProfile> available() {
        return specialistProfileRepository.findByActiveTrueAndAvailability(AvailabilityStatus.AVAILABLE);
    }

    public List<SpecialistProfile> all() {
        return specialistProfileRepository.findAll();
    }

    private void applyAssignment(
            Ticket ticket,
            SpecialistProfile specialist,
            SpecialistProfile previous,
            UserAccount actor,
            String reason,
            String eventType
    ) {
        if (previous != null && !previous.getId().equals(specialist.getId())) {
            previous.setCurrentWorkload(Math.max(0, previous.getCurrentWorkload() - 1));
        }
        if (previous == null || !previous.getId().equals(specialist.getId())) {
            specialist.setCurrentWorkload(specialist.getCurrentWorkload() + 1);
        }
        ticket.setAssignedSpecialist(specialist);
        ticket.setAssignedTeam(specialist.getTeam());
        ticket.setCurrentSupportLevel(specialist.getSupportLevel());
        TicketAssignmentHistory history = new TicketAssignmentHistory();
        history.setTicket(ticket);
        history.setFromSpecialist(previous);
        history.setToSpecialist(specialist);
        history.setSupportLevel(specialist.getSupportLevel());
        history.setEventType(eventType);
        history.setReason(reason);
        assignmentHistoryRepository.save(history);
        auditService.record(actor, ticket, AuditAction.ASSIGNMENT,
                previous != null ? previous.getUser().fullName() : null,
                specialist.getUser().fullName(), reason);
        notificationService.notify(ticket.getRequester(), ticket, NotificationType.TICKET_ASSIGNED,
                "Ticket " + ticket.getTicketNumber() + " assigned to " + specialist.getUser().fullName());
        notificationService.notify(specialist.getUser(), ticket, NotificationType.TICKET_ASSIGNED,
                "You were assigned " + ticket.getTicketNumber());
    }

    private SpecialistProfile validateSpecialist(Ticket ticket, Long specialistId, boolean escalation) {
        SpecialistProfile specialist = specialistProfileRepository.findById(specialistId)
                .orElseThrow(() -> new NotFoundException("Specialist not found"));
        if (!specialist.isActive() || specialist.getUser().getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Specialist is not active");
        }
        if (specialist.getAvailability() != AvailabilityStatus.AVAILABLE) {
            throw new BadRequestException("Specialist is not available");
        }
        String requiredSkill = requiredSkill(ticket);
        if (requiredSkill != null && !specialistSkillRepository.existsBySpecialistIdAndSkill_NameIgnoreCase(specialist.getId(), requiredSkill)) {
            throw new BadRequestException("Specialist does not have required skill: " + requiredSkill);
        }
        if (ticket.getCategory() != null && specialist.getTeam() != null
                && specialist.getTeam().getPrimaryCategory() != null
                && !specialist.getTeam().getPrimaryCategory().getId().equals(ticket.getCategory().getId())
                && !escalation) {
            throw new BadRequestException("Specialist team does not support this category");
        }
        return specialist;
    }

    private String requiredSkill(Ticket ticket) {
        if (ticket.getSubcategory() != null && ticket.getSubcategory().getRequiredSkillName() != null) {
            return ticket.getSubcategory().getRequiredSkillName();
        }
        if (ticket.getCategory() != null) {
            return ticket.getCategory().getRequiredSkillName();
        }
        return null;
    }

    private boolean isHigher(SupportLevel from, SupportLevel to) {
        return to.ordinal() > from.ordinal();
    }

    public List<SpecialistSkill> skills(Long specialistId) {
        return specialistSkillRepository.findBySpecialistId(specialistId);
    }

    public List<TicketAssignmentHistory> assignmentHistory(Long ticketId) {
        return assignmentHistoryRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }

    public List<TicketEscalationHistory> escalationHistory(Long ticketId) {
        return escalationHistoryRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }

    private Ticket lock(Long ticketId) {
        return ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
    }
}
