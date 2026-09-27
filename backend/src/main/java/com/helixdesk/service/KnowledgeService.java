package com.helixdesk.service;

import com.helixdesk.dto.KnowledgeDtos;
import com.helixdesk.entity.KnowledgeArticle;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.ArticleStatus;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.KnowledgeArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KnowledgeService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final CatalogService catalogService;

    public KnowledgeService(KnowledgeArticleRepository knowledgeArticleRepository, CatalogService catalogService) {
        this.knowledgeArticleRepository = knowledgeArticleRepository;
        this.catalogService = catalogService;
    }

    public List<KnowledgeArticle> search(String query) {
        if (query == null || query.isBlank()) {
            return knowledgeArticleRepository.findByStatus(ArticleStatus.PUBLISHED);
        }
        return knowledgeArticleRepository.searchPublished(query.trim());
    }

    public List<KnowledgeArticle> all() {
        return knowledgeArticleRepository.findAll();
    }

    public KnowledgeArticle get(Long id) {
        return knowledgeArticleRepository.findById(id).orElseThrow(() -> new NotFoundException("Article not found"));
    }

    @Transactional
    public KnowledgeArticle save(Long id, KnowledgeDtos.ArticleRequest request, UserAccount actor) {
        KnowledgeArticle article = id == null ? new KnowledgeArticle() : get(id);
        article.setTitle(request.title());
        article.setContent(request.content());
        article.setTags(request.tags());
        article.setStatus(request.status() == null ? ArticleStatus.DRAFT : request.status());
        if (request.categoryId() != null) {
            article.setCategory(catalogService.requireCategory(request.categoryId()));
        }
        if (request.subcategoryId() != null) {
            article.setSubcategory(catalogService.requireSubcategory(request.subcategoryId()));
        }
        if (id == null) {
            article.setCreatedBy(actor);
        }
        article.setUpdatedBy(actor);
        return knowledgeArticleRepository.save(article);
    }
}
