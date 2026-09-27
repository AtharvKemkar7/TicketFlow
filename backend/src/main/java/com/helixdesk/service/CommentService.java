package com.helixdesk.service;

import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.TicketComment;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AuditAction;
import com.helixdesk.enums.CommentType;
import com.helixdesk.enums.NotificationType;
import com.helixdesk.enums.RoleType;
import com.helixdesk.exception.ForbiddenException;
import com.helixdesk.repository.TicketCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketService ticketService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public CommentService(
            TicketCommentRepository ticketCommentRepository,
            TicketService ticketService,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketService = ticketService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public List<TicketComment> list(Long ticketId, UserAccount actor) {
        Ticket ticket = ticketService.getVisible(ticketId, actor);
        if (actor.getRole() == RoleType.USER) {
            return ticketCommentRepository.findByTicketIdAndTypeOrderByCreatedAtAsc(ticket.getId(), CommentType.PUBLIC_COMMENT);
        }
        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());
    }

    @Transactional
    public TicketComment add(Long ticketId, UserAccount actor, String body, boolean internal) {
        Ticket ticket = ticketService.getVisible(ticketId, actor);
        if (internal && actor.getRole() == RoleType.USER) {
            throw new ForbiddenException("Users cannot create internal notes");
        }
        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(actor);
        comment.setType(internal ? CommentType.INTERNAL_NOTE : CommentType.PUBLIC_COMMENT);
        comment.setBody(body);
        ticketCommentRepository.save(comment);
        auditService.record(actor, ticket, AuditAction.COMMENT_ADDED, null, comment.getType().name(), null);
        if (!internal && ticket.getAssignedSpecialist() != null
                && !ticket.getAssignedSpecialist().getUser().getId().equals(actor.getId())) {
            notificationService.notify(ticket.getAssignedSpecialist().getUser(), ticket,
                    NotificationType.SPECIALIST_RESPONSE, "New comment on " + ticket.getTicketNumber());
        }
        if (!internal && !ticket.getRequester().getId().equals(actor.getId())) {
            notificationService.notify(ticket.getRequester(), ticket,
                    NotificationType.SPECIALIST_RESPONSE, "New response on " + ticket.getTicketNumber());
        }
        return comment;
    }
}
