package com.helixdesk.controller;

import com.helixdesk.dto.CatalogDtos;
import com.helixdesk.dto.KnowledgeDtos;
import com.helixdesk.dto.TicketDtos;
import com.helixdesk.dto.UserDtos;
import com.helixdesk.entity.AuditLog;
import com.helixdesk.entity.SlaPolicy;
import com.helixdesk.entity.SpecialistProfile;
import com.helixdesk.entity.SystemSetting;
import com.helixdesk.repository.SystemSettingRepository;
import com.helixdesk.security.CurrentUser;
import com.helixdesk.service.AssignmentService;
import com.helixdesk.service.AuditService;
import com.helixdesk.service.CatalogService;
import com.helixdesk.service.KnowledgeService;
import com.helixdesk.service.PriorityService;
import com.helixdesk.service.ReportService;
import com.helixdesk.service.SlaService;
import com.helixdesk.service.SpecialistAdminService;
import com.helixdesk.service.TicketService;
import com.helixdesk.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TicketService ticketService;
    private final UserAdminService userAdminService;
    private final CatalogService catalogService;
    private final SpecialistAdminService specialistAdminService;
    private final AssignmentService assignmentService;
    private final PriorityService priorityService;
    private final SlaService slaService;
    private final KnowledgeService knowledgeService;
    private final AuditService auditService;
    private final ReportService reportService;
    private final SystemSettingRepository systemSettingRepository;
    private final CurrentUser currentUser;

    public AdminController(
            TicketService ticketService,
            UserAdminService userAdminService,
            CatalogService catalogService,
            SpecialistAdminService specialistAdminService,
            AssignmentService assignmentService,
            PriorityService priorityService,
            SlaService slaService,
            KnowledgeService knowledgeService,
            AuditService auditService,
            ReportService reportService,
            SystemSettingRepository systemSettingRepository,
            CurrentUser currentUser
    ) {
        this.ticketService = ticketService;
        this.userAdminService = userAdminService;
        this.catalogService = catalogService;
        this.specialistAdminService = specialistAdminService;
        this.assignmentService = assignmentService;
        this.priorityService = priorityService;
        this.slaService = slaService;
        this.knowledgeService = knowledgeService;
        this.auditService = auditService;
        this.reportService = reportService;
        this.systemSettingRepository = systemSettingRepository;
        this.currentUser = currentUser;
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDtos.TicketResponse>> tickets() {
        return ResponseEntity.ok(ticketService.all().stream().map(TicketDtos.TicketResponse::from).toList());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDtos.UserResponse>> users() {
        return ResponseEntity.ok(userAdminService.all());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDtos.UserResponse> createUser(@Valid @RequestBody UserDtos.CreateUserRequest request) {
        return ResponseEntity.ok(userAdminService.create(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDtos.UserResponse> updateUser(@PathVariable Long id, @RequestBody UserDtos.UpdateUserRequest request) {
        return ResponseEntity.ok(userAdminService.update(id, request));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CatalogDtos.CategoryResponse>> categories() {
        return ResponseEntity.ok(catalogService.categories().stream().map(CatalogDtos.CategoryResponse::from).toList());
    }

    @PostMapping("/categories")
    public ResponseEntity<CatalogDtos.CategoryResponse> createCategory(@Valid @RequestBody CatalogDtos.CategoryRequest request) {
        return ResponseEntity.ok(CatalogDtos.CategoryResponse.from(catalogService.saveCategory(null, request)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CatalogDtos.CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CatalogDtos.CategoryRequest request) {
        return ResponseEntity.ok(CatalogDtos.CategoryResponse.from(catalogService.saveCategory(id, request)));
    }

    @PostMapping("/subcategories")
    public ResponseEntity<CatalogDtos.SubcategoryResponse> createSubcategory(@Valid @RequestBody CatalogDtos.SubcategoryRequest request) {
        return ResponseEntity.ok(CatalogDtos.SubcategoryResponse.from(catalogService.saveSubcategory(null, request)));
    }

    @GetMapping("/teams")
    public ResponseEntity<List<CatalogDtos.TeamResponse>> teams() {
        return ResponseEntity.ok(catalogService.teams().stream().map(CatalogDtos.TeamResponse::from).toList());
    }

    @PostMapping("/teams")
    public ResponseEntity<CatalogDtos.TeamResponse> createTeam(@Valid @RequestBody CatalogDtos.TeamRequest request) {
        return ResponseEntity.ok(CatalogDtos.TeamResponse.from(catalogService.saveTeam(null, request)));
    }

    @PutMapping("/teams/{id}")
    public ResponseEntity<CatalogDtos.TeamResponse> updateTeam(@PathVariable Long id, @Valid @RequestBody CatalogDtos.TeamRequest request) {
        return ResponseEntity.ok(CatalogDtos.TeamResponse.from(catalogService.saveTeam(id, request)));
    }

    @GetMapping("/skills")
    public ResponseEntity<List<CatalogDtos.SkillResponse>> skills() {
        return ResponseEntity.ok(catalogService.skills().stream().map(CatalogDtos.SkillResponse::from).toList());
    }

    @PostMapping("/skills")
    public ResponseEntity<CatalogDtos.SkillResponse> createSkill(@Valid @RequestBody CatalogDtos.SkillRequest request) {
        return ResponseEntity.ok(CatalogDtos.SkillResponse.from(catalogService.saveSkill(null, request)));
    }

    @GetMapping("/specialists")
    public ResponseEntity<List<CatalogDtos.SpecialistResponse>> specialists() {
        return ResponseEntity.ok(specialistAdminService.all().stream().map(this::toSpecialist).toList());
    }

    @PostMapping("/specialists")
    public ResponseEntity<CatalogDtos.SpecialistResponse> createSpecialist(@Valid @RequestBody CatalogDtos.SpecialistRequest request) {
        return ResponseEntity.ok(toSpecialist(specialistAdminService.save(null, request)));
    }

    @PutMapping("/specialists/{id}")
    public ResponseEntity<CatalogDtos.SpecialistResponse> updateSpecialist(@PathVariable Long id, @Valid @RequestBody CatalogDtos.SpecialistRequest request) {
        return ResponseEntity.ok(toSpecialist(specialistAdminService.save(id, request)));
    }

    @GetMapping("/priority-rules")
    public ResponseEntity<List<Map<String, Object>>> priorityRules() {
        return ResponseEntity.ok(priorityService.allRules().stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("impact", r.getImpact());
            map.put("urgency", r.getUrgency());
            map.put("priority", r.getPriority());
            return map;
        }).toList());
    }

    @PostMapping("/priority-rules")
    public ResponseEntity<Map<String, Object>> upsertPriority(@Valid @RequestBody CatalogDtos.PriorityRuleRequest request) {
        var rule = priorityService.upsert(request.impact(), request.urgency(), request.priority());
        return ResponseEntity.ok(Map.of("id", rule.getId(), "priority", rule.getPriority()));
    }

    @GetMapping("/sla")
    public ResponseEntity<List<Map<String, Object>>> sla() {
        return ResponseEntity.ok(slaService.all().stream().map(this::toSla).toList());
    }

    @PostMapping("/sla")
    public ResponseEntity<Map<String, Object>> upsertSla(@Valid @RequestBody CatalogDtos.SlaPolicyRequest request) {
        SlaPolicy policy = slaService.upsert(request.priority(), request.responseTargetMinutes(),
                request.resolutionTargetMinutes(), request.atRiskThresholdMinutes());
        return ResponseEntity.ok(toSla(policy));
    }

    @GetMapping("/knowledge")
    public ResponseEntity<List<KnowledgeDtos.ArticleResponse>> knowledge() {
        return ResponseEntity.ok(knowledgeService.all().stream().map(KnowledgeDtos.ArticleResponse::from).toList());
    }

    @PostMapping("/knowledge")
    public ResponseEntity<KnowledgeDtos.ArticleResponse> createArticle(@Valid @RequestBody KnowledgeDtos.ArticleRequest request) {
        return ResponseEntity.ok(KnowledgeDtos.ArticleResponse.from(knowledgeService.save(null, request, currentUser.requireUser())));
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> reports() {
        slaService.refreshOpenTickets();
        return ResponseEntity.ok(reportService.dashboard());
    }

    @GetMapping("/audit")
    public ResponseEntity<List<Map<String, Object>>> audit() {
        return ResponseEntity.ok(auditService.all().stream().map(this::toAudit).toList());
    }

    @GetMapping("/settings")
    public ResponseEntity<List<Map<String, String>>> settings() {
        return ResponseEntity.ok(systemSettingRepository.findAll().stream()
                .map(s -> Map.of("key", s.getSettingKey(), "value", s.getSettingValue() == null ? "" : s.getSettingValue()))
                .toList());
    }

    @PostMapping("/settings")
    public ResponseEntity<Map<String, String>> saveSetting(@RequestBody Map<String, String> body) {
        SystemSetting setting = systemSettingRepository.findById(body.get("key")).orElseGet(SystemSetting::new);
        setting.setSettingKey(body.get("key"));
        setting.setSettingValue(body.get("value"));
        systemSettingRepository.save(setting);
        return ResponseEntity.ok(Map.of("key", setting.getSettingKey(), "value", setting.getSettingValue() == null ? "" : setting.getSettingValue()));
    }

    @PutMapping("/knowledge/{id}")
    public ResponseEntity<KnowledgeDtos.ArticleResponse> updateArticle(@PathVariable Long id, @Valid @RequestBody KnowledgeDtos.ArticleRequest request) {
        return ResponseEntity.ok(KnowledgeDtos.ArticleResponse.from(knowledgeService.save(id, request, currentUser.requireUser())));
    }

    private CatalogDtos.SpecialistResponse toSpecialist(SpecialistProfile profile) {
        List<String> skills = assignmentService.skills(profile.getId()).stream().map(s -> s.getSkill().getName()).toList();
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

    private Map<String, Object> toSla(SlaPolicy policy) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", policy.getId());
        map.put("priority", policy.getPriority());
        map.put("responseTargetMinutes", policy.getResponseTargetMinutes());
        map.put("resolutionTargetMinutes", policy.getResolutionTargetMinutes());
        map.put("atRiskThresholdMinutes", policy.getAtRiskThresholdMinutes());
        return map;
    }

    private Map<String, Object> toAudit(AuditLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("actor", log.getActorName());
        map.put("action", log.getAction());
        map.put("ticketId", log.getTicket() == null ? null : log.getTicket().getId());
        map.put("previousValue", log.getPreviousValue());
        map.put("newValue", log.getNewValue());
        map.put("reason", log.getReason());
        map.put("createdAt", log.getCreatedAt());
        return map;
    }
}
