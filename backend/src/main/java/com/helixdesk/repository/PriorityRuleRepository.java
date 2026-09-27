package com.helixdesk.repository;

import com.helixdesk.entity.PriorityRule;
import com.helixdesk.enums.Impact;
import com.helixdesk.enums.Urgency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriorityRuleRepository extends JpaRepository<PriorityRule, Long> {
    Optional<PriorityRule> findByImpactAndUrgency(Impact impact, Urgency urgency);
}
