package com.helixdesk.repository;

import com.helixdesk.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {
    List<Subcategory> findByCategoryId(Long categoryId);
    Optional<Subcategory> findByNameIgnoreCaseAndCategoryId(String name, Long categoryId);
}
