package com.helixdesk.repository;

import com.helixdesk.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByNameIgnoreCase(String name);
    List<Team> findByActiveTrue();
    List<Team> findByPrimaryCategoryIdAndActiveTrue(Long categoryId);
}
