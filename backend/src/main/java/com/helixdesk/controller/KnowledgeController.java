package com.helixdesk.controller;

import com.helixdesk.dto.KnowledgeDtos;
import com.helixdesk.security.CurrentUser;
import com.helixdesk.service.KnowledgeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final CurrentUser currentUser;

    public KnowledgeController(KnowledgeService knowledgeService, CurrentUser currentUser) {
        this.knowledgeService = knowledgeService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeDtos.ArticleResponse>> search(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(knowledgeService.search(q).stream().map(KnowledgeDtos.ArticleResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeDtos.ArticleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(KnowledgeDtos.ArticleResponse.from(knowledgeService.get(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KnowledgeDtos.ArticleResponse> create(@Valid @RequestBody KnowledgeDtos.ArticleRequest request) {
        return ResponseEntity.ok(KnowledgeDtos.ArticleResponse.from(
                knowledgeService.save(null, request, currentUser.requireUser())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KnowledgeDtos.ArticleResponse> update(@PathVariable Long id, @Valid @RequestBody KnowledgeDtos.ArticleRequest request) {
        return ResponseEntity.ok(KnowledgeDtos.ArticleResponse.from(
                knowledgeService.save(id, request, currentUser.requireUser())));
    }
}
