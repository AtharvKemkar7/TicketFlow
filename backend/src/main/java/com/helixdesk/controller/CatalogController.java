package com.helixdesk.controller;

import com.helixdesk.dto.CatalogDtos;
import com.helixdesk.service.AssignmentService;
import com.helixdesk.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;
    private final AssignmentService assignmentService;

    public CatalogController(CatalogService catalogService, AssignmentService assignmentService) {
        this.catalogService = catalogService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CatalogDtos.CategoryResponse>> categories() {
        return ResponseEntity.ok(catalogService.categories().stream().map(CatalogDtos.CategoryResponse::from).toList());
    }

    @GetMapping("/subcategories")
    public ResponseEntity<List<CatalogDtos.SubcategoryResponse>> subcategories(@RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(catalogService.subcategories(categoryId).stream().map(CatalogDtos.SubcategoryResponse::from).toList());
    }

    @GetMapping("/specialists")
    public ResponseEntity<List<CatalogDtos.SpecialistResponse>> specialists() {
        return ResponseEntity.ok(assignmentService.all().stream().map(this::toSpecialist).toList());
    }

    @GetMapping("/specialists/available")
    public ResponseEntity<List<CatalogDtos.SpecialistResponse>> available() {
        return ResponseEntity.ok(assignmentService.available().stream().map(this::toSpecialist).toList());
    }

    private CatalogDtos.SpecialistResponse toSpecialist(com.helixdesk.entity.SpecialistProfile profile) {
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
}
