package com.helixdesk.controller;

import com.helixdesk.dto.TicketDtos;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.TicketAttachment;
import com.helixdesk.entity.TicketComment;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.security.CurrentUser;
import com.helixdesk.service.AssignmentService;
import com.helixdesk.service.AttachmentService;
import com.helixdesk.service.AuditService;
import com.helixdesk.service.CommentService;
import com.helixdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;
    private final AttachmentService attachmentService;
    private final AssignmentService assignmentService;
    private final AuditService auditService;
    private final CurrentUser currentUser;

    public TicketController(
            TicketService ticketService,
            CommentService commentService,
            AttachmentService attachmentService,
            AssignmentService assignmentService,
            AuditService auditService,
            CurrentUser currentUser
    ) {
        this.ticketService = ticketService;
        this.commentService = commentService;
        this.attachmentService = attachmentService;
        this.assignmentService = assignmentService;
        this.auditService = auditService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<TicketDtos.TicketResponse> create(@Valid @RequestBody TicketDtos.CreateTicketRequest request) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(ticketService.create(currentUser.requireUser(), request)));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TicketDtos.TicketResponse>> mine() {
        return ResponseEntity.ok(ticketService.mine(currentUser.requireUser()).stream().map(TicketDtos.TicketResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDtos.TicketResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(ticketService.getVisible(id, currentUser.requireUser())));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Map<String, Object>> comment(@PathVariable Long id, @Valid @RequestBody TicketDtos.CommentRequest request) {
        TicketComment comment = commentService.add(id, currentUser.requireUser(), request.body(), request.internal());
        return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "type", comment.getType(),
                "body", comment.getBody(),
                "author", comment.getAuthor().fullName(),
                "createdAt", comment.getCreatedAt()
        ));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Map<String, Object>>> comments(@PathVariable Long id) {
        UserAccount actor = currentUser.requireUser();
        return ResponseEntity.ok(commentService.list(id, actor).stream().map(c -> Map.<String, Object>of(
                "id", c.getId(),
                "type", c.getType(),
                "body", c.getBody(),
                "author", c.getAuthor().fullName(),
                "createdAt", c.getCreatedAt()
        )).toList());
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<Map<String, Object>> attach(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        TicketAttachment attachment = attachmentService.store(id, currentUser.requireUser(), file);
        return ResponseEntity.ok(Map.of(
                "id", attachment.getId(),
                "filename", attachment.getOriginalFilename(),
                "sizeBytes", attachment.getSizeBytes()
        ));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<Map<String, Object>>> attachments(@PathVariable Long id) {
        return ResponseEntity.ok(attachmentService.list(id, currentUser.requireUser()).stream().map(a -> Map.<String, Object>of(
                "id", a.getId(),
                "filename", a.getOriginalFilename(),
                "sizeBytes", a.getSizeBytes(),
                "contentType", a.getContentType() == null ? "" : a.getContentType()
        )).toList());
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<TicketDtos.TicketResponse> assign(@PathVariable Long id, @Valid @RequestBody TicketDtos.AssignRequest request) {
        Ticket ticket = assignmentService.assign(id, request.specialistId(), currentUser.requireUser(), request.reason());
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(ticket));
    }

    @PostMapping("/{id}/reassign")
    public ResponseEntity<TicketDtos.TicketResponse> reassign(@PathVariable Long id, @Valid @RequestBody TicketDtos.ReassignRequest request) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(
                assignmentService.reassign(id, request.specialistId(), currentUser.requireUser(), request.reason())));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<TicketDtos.TicketResponse> escalate(@PathVariable Long id, @Valid @RequestBody TicketDtos.EscalateRequest request) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(
                assignmentService.escalate(id, request.specialistId(), currentUser.requireUser(), request.reason(), request.notes())));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<TicketDtos.TicketResponse> resolve(@PathVariable Long id, @Valid @RequestBody TicketDtos.ResolveRequest request) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(
                ticketService.resolve(id, currentUser.requireUser(), request.resolutionSummary())));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<TicketDtos.TicketResponse> reopen(@PathVariable Long id, @RequestBody(required = false) TicketDtos.ConfirmRequest request) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(
                ticketService.reopen(id, currentUser.requireUser(), request == null ? null : request.feedback())));
    }

    @PostMapping("/{id}/confirm-resolution")
    public ResponseEntity<TicketDtos.TicketResponse> confirm(@PathVariable Long id, @RequestBody TicketDtos.ConfirmRequest request) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(
                ticketService.confirm(id, currentUser.requireUser(), request.accepted(), request.feedback())));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketDtos.TicketResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(ticketService.cancel(id, currentUser.requireUser())));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<TicketDtos.TicketResponse> start(@PathVariable Long id) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(ticketService.startWork(id, currentUser.requireUser())));
    }

    @PostMapping("/{id}/wait")
    public ResponseEntity<TicketDtos.TicketResponse> waitForUser(@PathVariable Long id) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(ticketService.waitForUser(id, currentUser.requireUser())));
    }

    @PostMapping("/{id}/category")
    public ResponseEntity<TicketDtos.TicketResponse> category(@PathVariable Long id, @Valid @RequestBody TicketDtos.ChangeCategoryRequest request) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(
                ticketService.changeCategory(id, currentUser.requireUser(), request.categoryId(), request.subcategoryId(), request.reason())));
    }

    @PostMapping("/{id}/priority")
    public ResponseEntity<TicketDtos.TicketResponse> priority(@PathVariable Long id, @Valid @RequestBody TicketDtos.ChangePriorityRequest request) {
        return ResponseEntity.ok(TicketDtos.TicketResponse.from(
                ticketService.changePriority(id, currentUser.requireUser(), request.priority(), request.reason())));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<Map<String, Object>>> history(@PathVariable Long id) {
        ticketService.getVisible(id, currentUser.requireUser());
        return ResponseEntity.ok(auditService.forTicket(id).stream().map(log -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", log.getId());
            map.put("actor", log.getActorName());
            map.put("action", log.getAction());
            map.put("previousValue", log.getPreviousValue());
            map.put("newValue", log.getNewValue());
            map.put("reason", log.getReason());
            map.put("createdAt", log.getCreatedAt());
            return map;
        }).toList());
    }

    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<Map<String, Object>>> assignments(@PathVariable Long id) {
        ticketService.getVisible(id, currentUser.requireUser());
        return ResponseEntity.ok(assignmentService.assignmentHistory(id).stream().map(h -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", h.getId());
            map.put("eventType", h.getEventType());
            map.put("fromSpecialist", h.getFromSpecialist() == null ? null : h.getFromSpecialist().getUser().fullName());
            map.put("toSpecialist", h.getToSpecialist() == null ? null : h.getToSpecialist().getUser().fullName());
            map.put("supportLevel", h.getSupportLevel());
            map.put("reason", h.getReason());
            map.put("createdAt", h.getCreatedAt());
            return map;
        }).toList());
    }

    @GetMapping("/{id}/escalations")
    public ResponseEntity<List<Map<String, Object>>> escalations(@PathVariable Long id) {
        ticketService.getVisible(id, currentUser.requireUser());
        return ResponseEntity.ok(assignmentService.escalationHistory(id).stream().map(h -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", h.getId());
            map.put("fromLevel", h.getFromLevel());
            map.put("toLevel", h.getToLevel());
            map.put("fromSpecialist", h.getFromSpecialist() == null ? null : h.getFromSpecialist().getUser().fullName());
            map.put("toSpecialist", h.getToSpecialist() == null ? null : h.getToSpecialist().getUser().fullName());
            map.put("reason", h.getReason());
            map.put("notes", h.getNotes());
            map.put("createdAt", h.getCreatedAt());
            return map;
        }).toList());
    }
}
