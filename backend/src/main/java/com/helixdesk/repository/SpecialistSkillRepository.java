package com.helixdesk.repository;

import com.helixdesk.entity.SpecialistSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialistSkillRepository extends JpaRepository<SpecialistSkill, Long> {
    List<SpecialistSkill> findBySpecialistId(Long specialistId);
    boolean existsBySpecialistIdAndSkill_NameIgnoreCase(Long specialistId, String skillName);
}
