package com.helixdesk.dto;

import com.helixdesk.entity.KnowledgeArticle;
import com.helixdesk.enums.ArticleStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public final class KnowledgeDtos {
    private KnowledgeDtos() {
    }

    public record ArticleRequest(
            @NotBlank String title,
            @NotBlank String content,
            Long categoryId,
            Long subcategoryId,
            String tags,
            ArticleStatus status
    ) {
    }

    public record ArticleResponse(
            Long id,
            String title,
            String content,
            Long categoryId,
            String categoryName,
            Long subcategoryId,
            String subcategoryName,
            String tags,
            ArticleStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ArticleResponse from(KnowledgeArticle article) {
            return new ArticleResponse(
                    article.getId(),
                    article.getTitle(),
                    article.getContent(),
                    article.getCategory() != null ? article.getCategory().getId() : null,
                    article.getCategory() != null ? article.getCategory().getName() : null,
                    article.getSubcategory() != null ? article.getSubcategory().getId() : null,
                    article.getSubcategory() != null ? article.getSubcategory().getName() : null,
                    article.getTags(),
                    article.getStatus(),
                    article.getCreatedAt(),
                    article.getUpdatedAt()
            );
        }
    }
}
