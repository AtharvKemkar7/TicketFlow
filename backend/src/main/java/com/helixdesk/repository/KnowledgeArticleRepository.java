package com.helixdesk.repository;

import com.helixdesk.entity.KnowledgeArticle;
import com.helixdesk.enums.ArticleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    List<KnowledgeArticle> findByStatus(ArticleStatus status);

    @Query("""
            select a from KnowledgeArticle a
            where a.status = com.helixdesk.enums.ArticleStatus.PUBLISHED
              and (
                    lower(a.title) like lower(concat('%', :q, '%'))
                 or lower(a.content) like lower(concat('%', :q, '%'))
                 or lower(coalesce(a.tags, '')) like lower(concat('%', :q, '%'))
              )
            """)
    List<KnowledgeArticle> searchPublished(@Param("q") String query);
}
