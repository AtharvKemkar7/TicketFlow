package com.helixdesk.controller;

import com.helixdesk.dto.CatalogDtos;
import com.helixdesk.dto.TicketDtos;
import com.helixdesk.entity.SpecialistProfile;
import com.helixdesk.enums.AvailabilityStatus;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.repository.TicketRepository;
import com.helixdesk.security.CurrentUser;
import com.helixdesk.service.AssignmentService;
import com.helixdesk.service.SpecialistAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/specialist")
public class SpecialistController {

    private final SpecialistAdminService specialistAdminService;
    private final AssignmentService assignmentService;
    private final TicketRepository ticketRepository;
    private final CurrentUser currentUser;

    public SpecialistController(
            SpecialistAdminService specialistAdminService,
            AssignmentService assignmentService,
            TicketRepository ticketRepository,
            CurrentUser currentUser
    ) {
        this.specialistAdminService = specialistAdminService;
        this.assignmentService = assignmentService;
        this.ticketRepository = ticketRepository;
        this.currentUser = currentUser;
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDtos.TicketResponse>> myTickets() {
        SpecialistProfile profile = specialistAdminService.byUser(currentUser.requireUser().getId());
        return ResponseEntity.ok(ticketRepository.findByAssignedSpecialistIdOrderByCreatedAtDesc(profile.getId())
                .stream().map(TicketDtos.TicketResponse::from).toList());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        SpecialistProfile profile = specialistAdminService.byUser(currentUser.requireUser().getId());
        Map<String, Object> body = new LinkedHashMap<>();
        var tickets = ticketRepository.findByAssignedSpecialistIdOrderByCreatedAtDesc(profile.getId());
        body.put("assigned", tickets.size());
        body.put("open", tickets.stream().filter(t -> t.getStatus() != TicketStatus.CLOSED && t.getStatus() != TicketStatus.CANCELLED).count());
        body.put("inProgress", tickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count());
        body.put("waiting", tickets.stream().filter(t -> t.getStatus() == TicketStatus.WAITING_FOR_USER).count());
        body.put("resolved", tickets.stream().filter(t -> t.getStatus() == TicketStatus.USER_CONFIRMATION || t.getStatus() == TicketStatus.RESOLVED).count());
        body.put("reassigned", tickets.stream().filter(t -> t.getStatus() == TicketStatus.REASSIGNED).count());
        body.put("escalated", tickets.stream().filter(t -> t.getStatus() == TicketStatus.ESCALATED).count());
        body.put("highPriority", tickets.stream().filter(t -> t.getPriority() == com.helixdesk.enums.Priority.HIGH || t.getPriority() == com.helixdesk.enums.Priority.CRITICAL).count());
        body.put("slaAtRisk", tickets.stream().filter(t -> t.getSlaState() == com.helixdesk.enums.SlaState.AT_RISK || t.getSlaState() == com.helixdesk.enums.SlaState.BREACHED).count());
        body.put("workload", profile.getCurrentWorkload());
        body.put("level", profile.getSupportLevel());
        body.put("availability", profile.getAvailability());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/profile")
    public ResponseEntity<CatalogDtos.SpecialistResponse> profile() {
        SpecialistProfile profile = specialistAdminService.byUser(currentUser.requireUser().getId());
        List<String> skills = assignmentService.skills(profile.getId()).stream().map(s -> s.getSkill().getName()).toList();
        return ResponseEntity.ok(toResponse(profile, skills));
    }

    @PutMapping("/availability")
    public ResponseEntity<CatalogDtos.SpecialistResponse> availability(@RequestParam AvailabilityStatus status) {
        SpecialistProfile profile = specialistAdminService.updateAvailability(currentUser.requireUser().getId(), status);
        List<String> skills = assignmentService.skills(profile.getId()).stream().map(s -> s.getSkill().getName()).toList();
        return ResponseEntity.ok(toResponse(profile, skills));
    }

    private CatalogDtos.SpecialistResponse toResponse(SpecialistProfile profile, List<String> skills) {
        return new CatalogDtos.SpecialistResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().fullName(),
                profile.getUser().getEmail(),
                profile.getSupportLevel(),
                profile.getAvailability(),
                profile.getCurrentWorkload(),
                profile.isActive(),
                profile.getTeam() != null ? profile.getTeam().getId() : null,
                profile.getTeam() != null ? profile.getTeam().getName() : null,
                skills
        );
    }
}
