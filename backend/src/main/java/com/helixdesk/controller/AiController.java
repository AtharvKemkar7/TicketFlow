package com.helixdesk.controller;

import com.helixdesk.dto.AiDtos;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.security.CurrentUser;
import com.helixdesk.service.IntakeAgentService;
import com.helixdesk.service.ResolutionAgentService;
import com.helixdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final IntakeAgentService intakeAgentService;
    private final ResolutionAgentService resolutionAgentService;
    private final TicketService ticketService;
    private final CurrentUser currentUser;

    public AiController(
            IntakeAgentService intakeAgentService,
            ResolutionAgentService resolutionAgentService,
            TicketService ticketService,
            CurrentUser currentUser
    ) {
        this.intakeAgentService = intakeAgentService;
        this.resolutionAgentService = resolutionAgentService;
        this.ticketService = ticketService;
        this.currentUser = currentUser;
    }

    @PostMapping("/intake")
    public ResponseEntity<AiDtos.IntakeMessageResponse> intake(@Valid @RequestBody AiDtos.IntakeMessageRequest request) {
        return ResponseEntity.ok(intakeAgentService.converse(currentUser.requireUser(), request));
    }

    @PostMapping("/tickets/{id}/resolution")
    public ResponseEntity<AiDtos.ResolutionMessageResponse> resolution(
            @PathVariable Long id,
            @Valid @RequestBody AiDtos.ResolutionMessageRequest request,
            @RequestParam(defaultValue = "false") boolean requestHuman
    ) {
        UserAccount actor = currentUser.requireUser();
        Ticket ticket = ticketService.getVisible(id, actor);
        return ResponseEntity.ok(resolutionAgentService.userFeedback(ticket, actor, request.message(), requestHuman));
    }

    @PostMapping("/tickets/{id}/request-specialist")
    public ResponseEntity<AiDtos.ResolutionMessageResponse> requestSpecialist(@PathVariable Long id) {
        UserAccount actor = currentUser.requireUser();
        Ticket ticket = ticketService.getVisible(id, actor);
        ticket.setHumanRequested(true);
        return ResponseEntity.ok(resolutionAgentService.userFeedback(ticket, actor, "I need a human specialist", true));
    }
}
