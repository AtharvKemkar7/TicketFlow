package com.helixdesk.service;

import com.helixdesk.dto.TicketDtos;
import com.helixdesk.entity.Category;
import com.helixdesk.entity.Subcategory;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AuditAction;
import com.helixdesk.enums.NotificationType;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.RoleType;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.exception.BadRequestException;
import com.helixdesk.exception.ForbiddenException;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Set;

@Service
public class TicketService {

    private static final Set<TicketStatus> CANCELLABLE = Set.of(
            TicketStatus.NEW, TicketStatus.AI_PROCESSING, TicketStatus.AI_ATTEMPTING_RESOLUTION,
            TicketStatus.HUMAN_REQUIRED, TicketStatus.WAITING_FOR_USER
    );

    private final TicketRepository ticketRepository;
    private final CatalogService catalogService;
    private final PriorityService priorityService;
    private final SlaService slaService;
    private final TicketWorkflowService workflowService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public TicketService(
            TicketRepository ticketRepository,
            CatalogService catalogService,
            PriorityService priorityService,
            SlaService slaService,
            TicketWorkflowService workflowService,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.ticketRepository = ticketRepository;
        this.catalogService = catalogService;
        this.priorityService = priorityService;
        this.slaService = slaService;
        this.workflowService = workflowService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Ticket create(UserAccount requester, TicketDtos.CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTicketNumber(nextNumber());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setRequester(requester);
        ticket.setImpact(request.impact());
        ticket.setUrgency(request.urgency());
        ticket.setBusinessEffect(request.businessEffect());
        if (request.categoryId() != null) {
            ticket.setCategory(catalogService.requireCategory(request.categoryId()));
        }
        if (request.subcategoryId() != null) {
            ticket.setSubcategory(catalogService.requireSubcategory(request.subcategoryId()));
        }
        Priority priority = priorityService.calculate(request.impact(), request.urgency());
        ticket.setPriority(priority);
        ticket.setStatus(TicketStatus.NEW);
        slaService.applySla(ticket);
        ticketRepository.save(ticket);
        auditService.record(requester, ticket, AuditAction.TICKET_CREATED, null, ticket.getTicketNumber(), null);
        notificationService.notify(requester, ticket, NotificationType.TICKET_CREATED,
                "Ticket " + ticket.getTicketNumber() + " created");
        return ticket;
    }

    public Ticket getVisible(Long id, UserAccount actor) {
        Ticket ticket = require(id);
        assertCanView(ticket, actor);
        return ticket;
    }

    public List<Ticket> mine(UserAccount actor) {
        return ticketRepository.findByRequesterIdOrderByCreatedAtDesc(actor.getId());
    }

    public List<Ticket> all() {
        return ticketRepository.findAll();
    }

    @Transactional
    public Ticket changeStatus(Ticket ticket, TicketStatus to, UserAccount actor, String reason) {
        workflowService.assertTransition(ticket.getStatus(), to);
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(to);
        auditService.record(actor, ticket, AuditAction.STATUS_CHANGED, from.name(), to.name(), reason);
        return ticket;
    }

    @Transactional
    public Ticket cancel(Long id, UserAccount actor) {
        Ticket ticket = lockVisible(id, actor);
        if (actor.getRole() == RoleType.USER && !ticket.getRequester().getId().equals(actor.getId())) {
            throw new ForbiddenException("Cannot cancel another user's ticket");
        }
        if (!CANCELLABLE.contains(ticket.getStatus()) && actor.getRole() != RoleType.ADMIN) {
            throw new BadRequestException("Ticket cannot be cancelled in current status");
        }
        workflowService.assertTransition(ticket.getStatus(), TicketStatus.CANCELLED);
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.CANCELLED);
        auditService.record(actor, ticket, AuditAction.CANCELLATION, from.name(), TicketStatus.CANCELLED.name(), null);
        return ticket;
    }

    @Transactional
    public Ticket resolve(Long id, UserAccount actor, String summary) {
        Ticket ticket = lockVisible(id, actor);
        if (actor.getRole() == RoleType.USER) {
            throw new ForbiddenException("Users cannot resolve tickets directly");
        }
        workflowService.assertTransition(ticket.getStatus(), TicketStatus.RESOLVED);
        ticket.setResolutionSummary(summary);
        ticket.setResolvedAt(LocalDateTime.now());
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.RESOLVED);
        auditService.record(actor, ticket, AuditAction.RESOLUTION, from.name(), TicketStatus.RESOLVED.name(), summary);
        workflowService.assertTransition(ticket.getStatus(), TicketStatus.USER_CONFIRMATION);
        ticket.setStatus(TicketStatus.USER_CONFIRMATION);
        notificationService.notify(ticket.getRequester(), ticket, NotificationType.RESOLUTION_SUBMITTED,
                "Resolution submitted for " + ticket.getTicketNumber());
        return ticket;
    }

    @Transactional
    public Ticket confirm(Long id, UserAccount actor, boolean accepted, String feedback) {
        Ticket ticket = lockVisible(id, actor);
        if (!ticket.getRequester().getId().equals(actor.getId()) && actor.getRole() != RoleType.ADMIN) {
            throw new ForbiddenException("Only the requester can confirm resolution");
        }
        if (accepted) {
            workflowService.assertTransition(ticket.getStatus(), TicketStatus.CLOSED);
            TicketStatus from = ticket.getStatus();
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setClosedAt(LocalDateTime.now());
            auditService.record(actor, ticket, AuditAction.CLOSURE, from.name(), TicketStatus.CLOSED.name(), feedback);
            notificationService.notify(ticket.getRequester(), ticket, NotificationType.TICKET_CLOSED,
                    "Ticket " + ticket.getTicketNumber() + " closed");
        } else {
            workflowService.assertTransition(ticket.getStatus(), TicketStatus.REOPENED);
            TicketStatus from = ticket.getStatus();
            ticket.setStatus(TicketStatus.REOPENED);
            auditService.record(actor, ticket, AuditAction.REOPENING, from.name(), TicketStatus.REOPENED.name(), feedback);
            notificationService.notify(ticket.getRequester(), ticket, NotificationType.TICKET_REOPENED,
                    "Ticket " + ticket.getTicketNumber() + " reopened");
        }
        return ticket;
    }

    @Transactional
    public Ticket reopen(Long id, UserAccount actor, String reason) {
        Ticket ticket = lockVisible(id, actor);
        if (actor.getRole() == RoleType.USER && !ticket.getRequester().getId().equals(actor.getId())) {
            throw new ForbiddenException("Cannot reopen another user's ticket");
        }
        workflowService.assertTransition(ticket.getStatus(), TicketStatus.REOPENED);
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.REOPENED);
        auditService.record(actor, ticket, AuditAction.REOPENING, from.name(), TicketStatus.REOPENED.name(), reason);
        return ticket;
    }

    @Transactional
    public Ticket changeCategory(Long id, UserAccount actor, Long categoryId, Long subcategoryId, String reason) {
        Ticket ticket = getVisible(id, actor);
        if (actor.getRole() == RoleType.USER) {
            throw new ForbiddenException("Users cannot recategorize tickets");
        }
        Category category = catalogService.requireCategory(categoryId);
        String previous = ticket.getCategory() != null ? ticket.getCategory().getName() : null;
        ticket.setCategory(category);
        if (subcategoryId != null) {
            Subcategory subcategory = catalogService.requireSubcategory(subcategoryId);
            ticket.setSubcategory(subcategory);
        }
        auditService.record(actor, ticket, AuditAction.CATEGORY_CHANGED, previous, category.getName(), reason);
        return ticket;
    }

    @Transactional
    public Ticket changePriority(Long id, UserAccount actor, Priority priority, String reason) {
        Ticket ticket = getVisible(id, actor);
        if (actor.getRole() != RoleType.ADMIN) {
            throw new ForbiddenException("Only admin can override priority");
        }
        Priority previous = ticket.getPriority();
        ticket.setPriority(priority);
        slaService.applySla(ticket);
        auditService.record(actor, ticket, AuditAction.PRIORITY_CHANGED, previous.name(), priority.name(), reason);
        return ticket;
    }

    @Transactional
    public Ticket startWork(Long id, UserAccount actor) {
        Ticket ticket = lockVisible(id, actor);
        workflowService.assertTransition(ticket.getStatus(), TicketStatus.IN_PROGRESS);
        if (ticket.getFirstRespondedAt() == null) {
            ticket.setFirstRespondedAt(LocalDateTime.now());
        }
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        auditService.record(actor, ticket, AuditAction.STATUS_CHANGED, from.name(), TicketStatus.IN_PROGRESS.name(), "Started work");
        return ticket;
    }

    @Transactional
    public Ticket waitForUser(Long id, UserAccount actor) {
        Ticket ticket = lockVisible(id, actor);
        workflowService.assertTransition(ticket.getStatus(), TicketStatus.WAITING_FOR_USER);
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.WAITING_FOR_USER);
        auditService.record(actor, ticket, AuditAction.STATUS_CHANGED, from.name(), TicketStatus.WAITING_FOR_USER.name(), "Waiting for user");
        return ticket;
    }

    public Ticket require(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    public Ticket lock(Long id) {
        return ticketRepository.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    private Ticket lockVisible(Long id, UserAccount actor) {
        Ticket ticket = lock(id);
        assertCanView(ticket, actor);
        return ticket;
    }

    public void assertCanView(Ticket ticket, UserAccount actor) {
        if (actor.getRole() == RoleType.ADMIN) {
            return;
        }
        if (actor.getRole() == RoleType.USER && !ticket.getRequester().getId().equals(actor.getId())) {
            throw new ForbiddenException("Cannot view another user's ticket");
        }
        if (actor.getRole() == RoleType.SPECIALIST) {
            boolean assigned = ticket.getAssignedSpecialist() != null
                    && ticket.getAssignedSpecialist().getUser() != null
                    && ticket.getAssignedSpecialist().getUser().getId().equals(actor.getId());
            if (!assigned) {
                throw new ForbiddenException("Specialists can only view assigned tickets");
            }
        }
    }

    private String nextNumber() {
        long count = ticketRepository.count() + 1001;
        return "HX-" + Year.now().getValue() + "-" + count;
    }
}
