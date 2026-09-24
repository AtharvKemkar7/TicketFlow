package com.helixdesk.dto;

import com.helixdesk.entity.Category;
import com.helixdesk.entity.Skill;
import com.helixdesk.entity.Subcategory;
import com.helixdesk.entity.Team;
import com.helixdesk.enums.AvailabilityStatus;
import com.helixdesk.enums.Impact;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.SupportLevel;
import com.helixdesk.enums.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class CatalogDtos {
    private CatalogDtos() {
    }

    public record CategoryResponse(Long id, String name, String description, boolean active, String requiredSkillName) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    category.isActive(),
                    category.getRequiredSkillName()
            );
        }
    }

    public record SubcategoryResponse(Long id, String name, Long categoryId, boolean active, String requiredSkillName) {
        public static SubcategoryResponse from(Subcategory subcategory) {
            return new SubcategoryResponse(
                    subcategory.getId(),
                    subcategory.getName(),
                    subcategory.getCategory().getId(),
                    subcategory.isActive(),
                    subcategory.getRequiredSkillName()
            );
        }
    }

    public record CategoryRequest(@NotBlank String name, String description, String requiredSkillName, boolean active) {
    }

    public record SubcategoryRequest(@NotBlank String name, @NotNull Long categoryId, String requiredSkillName, boolean active) {
    }

    public record TeamResponse(Long id, String name, String description, boolean active, Long primaryCategoryId, String primaryCategoryName) {
        public static TeamResponse from(Team team) {
            return new TeamResponse(
                    team.getId(),
                    team.getName(),
                    team.getDescription(),
                    team.isActive(),
                    team.getPrimaryCategory() != null ? team.getPrimaryCategory().getId() : null,
                    team.getPrimaryCategory() != null ? team.getPrimaryCategory().getName() : null
            );
        }
    }

    public record TeamRequest(@NotBlank String name, String description, Long primaryCategoryId, boolean active) {
    }

    public record SkillResponse(Long id, String name, String description, boolean active) {
        public static SkillResponse from(Skill skill) {
            return new SkillResponse(skill.getId(), skill.getName(), skill.getDescription(), skill.isActive());
        }
    }

    public record SkillRequest(@NotBlank String name, String description, boolean active) {
    }

    public record SpecialistResponse(
            Long id,
            Long userId,
            String fullName,
            String email,
            SupportLevel supportLevel,
            AvailabilityStatus availability,
            int currentWorkload,
            boolean active,
            Long teamId,
            String teamName,
            List<String> skills
    ) {
    }

    public record SpecialistRequest(
            @NotNull Long userId,
            Long teamId,
            SupportLevel supportLevel,
            AvailabilityStatus availability,
            boolean active,
            List<Long> skillIds
    ) {
    }

    public record PriorityRuleRequest(@NotNull Impact impact, @NotNull Urgency urgency, @NotNull Priority priority) {
    }

    public record SlaPolicyRequest(@NotNull Priority priority, int responseTargetMinutes, int resolutionTargetMinutes, int atRiskThresholdMinutes) {
    }
}
