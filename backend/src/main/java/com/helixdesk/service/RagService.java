package com.helixdesk.service;

import com.helixdesk.entity.KnowledgeArticle;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final KnowledgeService knowledgeService;

    public RagService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    public List<KnowledgeArticle> retrieve(String query, int limit) {
        List<KnowledgeArticle> published = knowledgeService.search(null);
        if (query == null || query.isBlank()) {
            return published.stream().limit(limit).toList();
        }
        List<String> tokens = Arrays.stream(query.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(t -> t.length() > 2)
                .toList();
        return published.stream()
                .sorted(Comparator.comparingInt((KnowledgeArticle a) -> score(a, tokens)).reversed())
                .filter(a -> score(a, tokens) > 0)
                .limit(limit)
                .toList();
    }

    public String buildContext(List<KnowledgeArticle> articles) {
        return articles.stream()
                .map(a -> "Title: " + a.getTitle() + "\n" + a.getContent())
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private int score(KnowledgeArticle article, List<String> tokens) {
        String hay = ((article.getTitle() == null ? "" : article.getTitle()) + " "
                + (article.getTags() == null ? "" : article.getTags()) + " "
                + (article.getContent() == null ? "" : article.getContent())).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String token : tokens) {
            if (hay.contains(token)) {
                score += hay.contains(article.getTitle() != null && article.getTitle().toLowerCase(Locale.ROOT).contains(token) ? token : "\0") ? 3 : 1;
                if (article.getTitle() != null && article.getTitle().toLowerCase(Locale.ROOT).contains(token)) {
                    score += 3;
                }
            }
        }
        return score;
    }
}
