package com.helixdesk.service;

import com.helixdesk.dto.AiDtos;
import com.helixdesk.entity.AiResolutionAttempt;
import com.helixdesk.entity.KnowledgeArticle;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AuditAction;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.repository.AiResolutionAttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ResolutionAgentService {

    private final RagService ragService;
    private final AiResolutionAttemptRepository attemptRepository;
    private final TicketWorkflowService workflowService;
    private final AuditService auditService;
    private final AssignmentAgentService assignmentAgentService;
    private final int maxAttempts;

    public ResolutionAgentService(
            RagService ragService,
            AiResolutionAttemptRepository attemptRepository,
            TicketWorkflowService workflowService,
            AuditService auditService,
            AssignmentAgentService assignmentAgentService,
            @Value("${helixdesk.ai.max-attempts:2}") int maxAttempts
    ) {
        this.ragService = ragService;
        this.attemptRepository = attemptRepository;
        this.workflowService = workflowService;
        this.auditService = auditService;
        this.assignmentAgentService = assignmentAgentService;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public AiDtos.ResolutionMessageResponse attempt(Ticket ticket, UserAccount actor, String userMessage, boolean userRequestedHuman) {
        String query = ticket.getTitle() + " " + ticket.getDescription() + " " + (userMessage == null ? "" : userMessage);
        List<KnowledgeArticle> articles = ragService.retrieve(query, 3);
        boolean critical = ticket.getPriority() == Priority.CRITICAL;
        boolean lowConfidence = articles.isEmpty();
        if (userRequestedHuman || ticket.isHumanRequested() || critical || lowConfidence
                || ticket.getAiAttemptCount() >= maxAttempts) {
            return handoff(ticket, actor, reason(userRequestedHuman, critical, lowConfidence, ticket.getAiAttemptCount()));
        }

        if (ticket.getStatus() == TicketStatus.NEW) {
            workflowService.assertTransition(ticket.getStatus(), TicketStatus.AI_PROCESSING);
            ticket.setStatus(TicketStatus.AI_PROCESSING);
        }
        if (ticket.getStatus() == TicketStatus.AI_PROCESSING) {
            workflowService.assertTransition(ticket.getStatus(), TicketStatus.AI_ATTEMPTING_RESOLUTION);
            ticket.setStatus(TicketStatus.AI_ATTEMPTING_RESOLUTION);
        }

        int attemptNumber = ticket.getAiAttemptCount() + 1;
        ticket.setAiAttemptCount(attemptNumber);
        KnowledgeArticle primary = articles.get(0);
        String solution = attemptNumber == 1
                ? "Attempt 1: " + firstSteps(primary)
                : "Attempt 2: " + alternateSteps(primary);
        AiResolutionAttempt attempt = new AiResolutionAttempt();
        attempt.setTicket(ticket);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setSolutionSummary(solution);
        attempt.setKnowledgeSources(primary.getTitle());
        attempt.setSuccessful(false);
        attempt.setHandedOff(false);
        attemptRepository.save(attempt);
        auditService.record(actor, ticket, AuditAction.AI_ATTEMPT, String.valueOf(attemptNumber - 1),
                String.valueOf(attemptNumber), primary.getTitle());
        String reply = solution + " Please try these steps and tell me if the issue is resolved.";
        return new AiDtos.ResolutionMessageResponse(reply, attemptNumber, false, null,
                articles.stream().map(KnowledgeArticle::getTitle).toList());
    }

    @Transactional
    public AiDtos.ResolutionMessageResponse userFeedback(Ticket ticket, UserAccount actor, String message, boolean requestHuman) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (requestHuman || lower.contains("specialist") || lower.contains("human")) {
            ticket.setHumanRequested(true);
            return handoff(ticket, actor, "User requested specialist");
        }
        if (lower.contains("fixed") || lower.contains("resolved") || lower.contains("working now") || lower.contains("yes it is fixed")) {
            if (ticket.getStatus() == TicketStatus.AI_ATTEMPTING_RESOLUTION) {
                workflowService.assertTransition(ticket.getStatus(), TicketStatus.USER_CONFIRMATION);
            }
            ticket.setResolutionSummary("User confirmed AI troubleshooting resolved the issue.");
            ticket.setStatus(TicketStatus.USER_CONFIRMATION);
            return new AiDtos.ResolutionMessageResponse(
                    "Glad that helped. Please confirm the resolution so I can close the ticket.",
                    ticket.getAiAttemptCount(), false, null, List.of());
        }
        return attempt(ticket, actor, message, false);
    }

    private AiDtos.ResolutionMessageResponse handoff(Ticket ticket, UserAccount actor, String reason) {
        ticket.setHumanRequested(true);
        if (ticket.getStatus() != TicketStatus.HUMAN_REQUIRED
                && ticket.getStatus() != TicketStatus.ASSIGNED
                && ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            if (ticket.getStatus() == TicketStatus.NEW) {
                workflowService.assertTransition(ticket.getStatus(), TicketStatus.HUMAN_REQUIRED);
            } else if (ticket.getStatus() == TicketStatus.AI_PROCESSING
                    || ticket.getStatus() == TicketStatus.AI_ATTEMPTING_RESOLUTION) {
                workflowService.assertTransition(ticket.getStatus(), TicketStatus.HUMAN_REQUIRED);
            }
            ticket.setStatus(TicketStatus.HUMAN_REQUIRED);
        }
        AiResolutionAttempt attempt = new AiResolutionAttempt();
        attempt.setTicket(ticket);
        attempt.setAttemptNumber(ticket.getAiAttemptCount());
        attempt.setHandedOff(true);
        attempt.setHandoffReason(reason);
        attempt.setSolutionSummary("Handed off to human specialist");
        attemptRepository.save(attempt);
        auditService.record(actor, ticket, AuditAction.AI_HANDOFF, TicketStatus.AI_ATTEMPTING_RESOLUTION.name(),
                TicketStatus.HUMAN_REQUIRED.name(), reason);
        assignmentAgentService.autoAssign(ticket, actor);
        return new AiDtos.ResolutionMessageResponse(
                "I am handing this to a human specialist. Reason: " + reason,
                ticket.getAiAttemptCount(), true, reason, List.of());
    }

    private String reason(boolean userRequested, boolean critical, boolean lowConfidence, int attempts) {
        if (userRequested) {
            return "User requested specialist";
        }
        if (critical) {
            return "Critical issue requires human intervention";
        }
        if (lowConfidence) {
            return "No reliable knowledge-base result";
        }
        if (attempts >= maxAttempts) {
            return "Maximum AI resolution attempts reached";
        }
        return "Same solution already failed";
    }

    private String firstSteps(KnowledgeArticle article) {
        return article.getContent().lines().limit(6).reduce("", (a, b) -> a.isBlank() ? b : a + " " + b);
    }

    private String alternateSteps(KnowledgeArticle article) {
        List<String> lines = article.getContent().lines().toList();
        if (lines.size() > 6) {
            return String.join(" ", lines.subList(Math.min(6, lines.size()), Math.min(12, lines.size())));
        }
        return "Check credentials, local network path, and related configuration described in " + article.getTitle();
    }
}
