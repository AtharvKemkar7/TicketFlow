package com.helixdesk.service;

import com.helixdesk.dto.CatalogDtos;
import com.helixdesk.entity.Category;
import com.helixdesk.entity.Skill;
import com.helixdesk.entity.Subcategory;
import com.helixdesk.entity.Team;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.CategoryRepository;
import com.helixdesk.repository.SkillRepository;
import com.helixdesk.repository.SubcategoryRepository;
import com.helixdesk.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final TeamRepository teamRepository;
    private final SkillRepository skillRepository;

    public CatalogService(
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository,
            TeamRepository teamRepository,
            SkillRepository skillRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.teamRepository = teamRepository;
        this.skillRepository = skillRepository;
    }

    public List<Category> categories() {
        return categoryRepository.findAll();
    }

    public List<Subcategory> subcategories(Long categoryId) {
        if (categoryId == null) {
            return subcategoryRepository.findAll();
        }
        return subcategoryRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Category saveCategory(Long id, CatalogDtos.CategoryRequest request) {
        Category category = id == null ? new Category() : categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        category.setName(request.name());
        category.setDescription(request.description());
        category.setRequiredSkillName(request.requiredSkillName());
        category.setActive(request.active());
        return categoryRepository.save(category);
    }

    @Transactional
    public Subcategory saveSubcategory(Long id, CatalogDtos.SubcategoryRequest request) {
        Subcategory subcategory = id == null ? new Subcategory() : subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subcategory not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        subcategory.setName(request.name());
        subcategory.setCategory(category);
        subcategory.setRequiredSkillName(request.requiredSkillName());
        subcategory.setActive(request.active());
        return subcategoryRepository.save(subcategory);
    }

    public List<Team> teams() {
        return teamRepository.findAll();
    }

    @Transactional
    public Team saveTeam(Long id, CatalogDtos.TeamRequest request) {
        Team team = id == null ? new Team() : teamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Team not found"));
        team.setName(request.name());
        team.setDescription(request.description());
        team.setActive(request.active());
        if (request.primaryCategoryId() != null) {
            team.setPrimaryCategory(categoryRepository.findById(request.primaryCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found")));
        }
        return teamRepository.save(team);
    }

    public List<Skill> skills() {
        return skillRepository.findAll();
    }

    @Transactional
    public Skill saveSkill(Long id, CatalogDtos.SkillRequest request) {
        Skill skill = id == null ? new Skill() : skillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Skill not found"));
        skill.setName(request.name());
        skill.setDescription(request.description());
        skill.setActive(request.active());
        return skillRepository.save(skill);
    }

    public Category requireCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
    }

    public Subcategory requireSubcategory(Long id) {
        return subcategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Subcategory not found"));
    }
}
