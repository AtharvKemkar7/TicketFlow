package com.helixdesk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helixdesk.dto.AiDtos;
import com.helixdesk.dto.TicketDtos;
import com.helixdesk.entity.Category;
import com.helixdesk.entity.IntakeSession;
import com.helixdesk.entity.Subcategory;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.Impact;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.enums.Urgency;
import com.helixdesk.exception.BadRequestException;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.CategoryRepository;
import com.helixdesk.repository.IntakeSessionRepository;
import com.helixdesk.repository.SubcategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class IntakeAgentService {

    private final IntakeSessionRepository intakeSessionRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final TicketService ticketService;
    private final ResolutionAgentService resolutionAgentService;
    private final AssignmentAgentService assignmentAgentService;
    private final PriorityService priorityService;
    private final ObjectMapper objectMapper;

    public IntakeAgentService(
            IntakeSessionRepository intakeSessionRepository,
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository,
            TicketService ticketService,
            ResolutionAgentService resolutionAgentService,
            AssignmentAgentService assignmentAgentService,
            PriorityService priorityService,
            ObjectMapper objectMapper
    ) {
        this.intakeSessionRepository = intakeSessionRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.ticketService = ticketService;
        this.resolutionAgentService = resolutionAgentService;
        this.assignmentAgentService = assignmentAgentService;
        this.priorityService = priorityService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiDtos.IntakeMessageResponse converse(UserAccount user, AiDtos.IntakeMessageRequest request) {
        IntakeSession session = request.sessionId() == null
                ? newSession(user)
                : intakeSessionRepository.findByIdAndUserId(request.sessionId(), user.getId())
                .orElseThrow(() -> new NotFoundException("Intake session not found"));
        List<Map<String, String>> history = readHistory(session);
        history.add(Map.of("role", "user", "content", request.message()));
        extract(session, request.message());

        String reply;
        boolean complete = false;
        Long ticketId = null;
        String ticketNumber = null;

        if (session.getImpact() == null) {
            reply = "Thanks. Are other employees also affected, or is this only happening for you?";
        } else if (session.getSuggestedCategory() == null) {
            reply = "I have the impact. Which area does this relate to: Network, Access, Hardware, Software, or Email?";
        } else if (session.getTitle() == null || session.getTitle().isBlank()) {
            session.setTitle(summarizeTitle(session.getProblemSummary() != null ? session.getProblemSummary() : request.message()));
            reply = "Got it. One last check: what business effect are you seeing if this stays unresolved?";
        } else if (!session.isComplete()) {
            Ticket ticket = createFromSession(user, session);
            session.setComplete(true);
            session.setCreatedTicket(ticket);
            complete = true;
            ticketId = ticket.getId();
            ticketNumber = ticket.getTicketNumber();
            Priority priority = ticket.getPriority();
            if (priority == Priority.CRITICAL) {
                ticketService.changeStatus(ticket, TicketStatus.HUMAN_REQUIRED, user, "Critical issue requires human");
                assignmentAgentService.autoAssign(ticket, user);
                reply = "This looks critical, so I created " + ticketNumber
                        + " and routed it to a human specialist immediately.";
            } else {
                ticketService.changeStatus(ticket, TicketStatus.AI_PROCESSING, user, "Intake complete");
                var resolution = resolutionAgentService.attempt(ticket, user, request.message(), false);
                reply = "I created ticket " + ticketNumber + ". " + resolution.reply();
            }
        } else {
            reply = "This intake session already created a ticket.";
            complete = true;
            if (session.getCreatedTicket() != null) {
                ticketId = session.getCreatedTicket().getId();
                ticketNumber = session.getCreatedTicket().getTicketNumber();
            }
        }

        history.add(Map.of("role", "assistant", "content", reply));
        session.setConversationJson(writeHistory(history));
        intakeSessionRepository.save(session);
        return new AiDtos.IntakeMessageResponse(
                session.getId(),
                reply,
                complete,
                session.getSuggestedCategory(),
                session.getSuggestedSubcategory(),
                session.getImpact(),
                session.getUrgency(),
                session.getTitle(),
                ticketId,
                ticketNumber
        );
    }

    private IntakeSession newSession(UserAccount user) {
        IntakeSession session = new IntakeSession();
        session.setUser(user);
        session.setConversationJson("[]");
        return intakeSessionRepository.save(session);
    }

    private void extract(IntakeSession session, String message) {
        String text = message.toLowerCase(Locale.ROOT);
        if (session.getProblemSummary() == null) {
            session.setProblemSummary(message);
        }
        if (containsAny(text, "entire company", "everyone", "all users", "production is down", "whole organization")) {
            session.setImpact(Impact.ORGANIZATION);
            session.setUrgency(Urgency.CRITICAL);
            session.setBusinessEffect("Organization-wide outage");
        } else if (containsAny(text, "department", "whole team", "my team")) {
            session.setImpact(Impact.DEPARTMENT);
            session.setUrgency(Urgency.HIGH);
        } else if (containsAny(text, "only me", "just me", "myself", "no one else", "nobody else")) {
            session.setImpact(Impact.INDIVIDUAL);
            if (session.getUrgency() == null) {
                session.setUrgency(Urgency.MEDIUM);
            }
        }
        if (containsAny(text, "vpn")) {
            session.setSuggestedCategory("NETWORK");
            session.setSuggestedSubcategory("VPN");
        } else if (containsAny(text, "wifi", "network", "internet")) {
            session.setSuggestedCategory("NETWORK");
            session.setSuggestedSubcategory("CONNECTIVITY");
        } else if (containsAny(text, "password", "login", "mfa", "account locked")) {
            session.setSuggestedCategory("ACCESS");
            session.setSuggestedSubcategory("CREDENTIALS");
        } else if (containsAny(text, "laptop", "printer", "keyboard")) {
            session.setSuggestedCategory("HARDWARE");
            session.setSuggestedSubcategory("DEVICE");
        } else if (containsAny(text, "outlook", "email")) {
            session.setSuggestedCategory("EMAIL");
            session.setSuggestedSubcategory("OUTLOOK");
        } else if (containsAny(text, "application", "software", "app crash")) {
            session.setSuggestedCategory("SOFTWARE");
            session.setSuggestedSubcategory("APPLICATION");
        }
        if (containsAny(text, "cannot work", "blocked", "urgent", "critical")) {
            session.setUrgency(Urgency.CRITICAL);
        }
        if (session.getBusinessEffect() == null && containsAny(text, "cannot", "blocked", "outage")) {
            session.setBusinessEffect(message);
        }
        if (session.getTitle() == null && message.length() > 12 && session.getSuggestedCategory() != null) {
            session.setTitle(summarizeTitle(message));
        }
    }

    private Ticket createFromSession(UserAccount user, IntakeSession session) {
        Long categoryId = null;
        Long subcategoryId = null;
        if (session.getSuggestedCategory() != null) {
            Category category = categoryRepository.findByNameIgnoreCase(session.getSuggestedCategory()).orElse(null);
            if (category != null) {
                categoryId = category.getId();
                if (session.getSuggestedSubcategory() != null) {
                    subcategoryId = subcategoryRepository
                            .findByNameIgnoreCaseAndCategoryId(session.getSuggestedSubcategory(), category.getId())
                            .map(Subcategory::getId)
                            .orElse(null);
                }
            }
        }
        Impact impact = session.getImpact() == null ? Impact.INDIVIDUAL : session.getImpact();
        Urgency urgency = session.getUrgency() == null ? Urgency.MEDIUM : session.getUrgency();
        priorityService.calculate(impact, urgency);
        TicketDtos.CreateTicketRequest create = new TicketDtos.CreateTicketRequest(
                session.getTitle() == null ? "IT support request" : session.getTitle(),
                session.getProblemSummary() == null ? "Intake conversation" : session.getProblemSummary(),
                categoryId,
                subcategoryId,
                impact,
                urgency,
                session.getBusinessEffect()
        );
        return ticketService.create(user, create);
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String summarizeTitle(String message) {
        String trimmed = message.trim();
        return trimmed.length() > 80 ? trimmed.substring(0, 80) : trimmed;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> readHistory(IntakeSession session) {
        try {
            if (session.getConversationJson() == null || session.getConversationJson().isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(session.getConversationJson(), List.class);
        } catch (JsonProcessingException ex) {
            return new ArrayList<>();
        }
    }

    private String writeHistory(List<Map<String, String>> history) {
        try {
            return objectMapper.writeValueAsString(history);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Unable to persist intake conversation");
        }
    }
}
