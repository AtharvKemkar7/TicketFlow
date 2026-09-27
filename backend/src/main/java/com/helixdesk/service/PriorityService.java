package com.helixdesk.service;

import com.helixdesk.entity.PriorityRule;
import com.helixdesk.enums.Impact;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.Urgency;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.PriorityRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PriorityService {

    private final PriorityRuleRepository priorityRuleRepository;

    public PriorityService(PriorityRuleRepository priorityRuleRepository) {
        this.priorityRuleRepository = priorityRuleRepository;
    }

    public Priority calculate(Impact impact, Urgency urgency) {
        return priorityRuleRepository.findByImpactAndUrgency(impact, urgency)
                .map(PriorityRule::getPriority)
                .orElseGet(() -> fallback(impact, urgency));
    }

    public List<PriorityRule> allRules() {
        return priorityRuleRepository.findAll();
    }

    @Transactional
    public PriorityRule upsert(Impact impact, Urgency urgency, Priority priority) {
        PriorityRule rule = priorityRuleRepository.findByImpactAndUrgency(impact, urgency)
                .orElseGet(PriorityRule::new);
        rule.setImpact(impact);
        rule.setUrgency(urgency);
        rule.setPriority(priority);
        return priorityRuleRepository.save(rule);
    }

    public PriorityRule get(Long id) {
        return priorityRuleRepository.findById(id).orElseThrow(() -> new NotFoundException("Priority rule not found"));
    }

    private Priority fallback(Impact impact, Urgency urgency) {
        if (impact == Impact.ORGANIZATION || urgency == Urgency.CRITICAL) {
            return Priority.CRITICAL;
        }
        if (impact == Impact.DEPARTMENT || urgency == Urgency.HIGH) {
            return Priority.HIGH;
        }
        if (impact == Impact.TEAM || urgency == Urgency.MEDIUM) {
            return Priority.MEDIUM;
        }
        return Priority.LOW;
    }
}
