package com.helixdesk.service;

import com.helixdesk.entity.SpecialistProfile;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.SupportLevel;
import com.helixdesk.repository.SpecialistProfileRepository;
import com.helixdesk.repository.SpecialistSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AssignmentAgentService {

    private final SpecialistProfileRepository specialistProfileRepository;
    private final SpecialistSkillRepository specialistSkillRepository;
    private final AssignmentService assignmentService;

    public AssignmentAgentService(
            SpecialistProfileRepository specialistProfileRepository,
            SpecialistSkillRepository specialistSkillRepository,
            AssignmentService assignmentService
    ) {
        this.specialistProfileRepository = specialistProfileRepository;
        this.specialistSkillRepository = specialistSkillRepository;
        this.assignmentService = assignmentService;
    }

    @Transactional
    public Ticket autoAssign(Ticket ticket, UserAccount actor) {
        SpecialistProfile match = recommend(ticket);
        if (match == null) {
            return ticket;
        }
        try {
            return assignmentService.assign(ticket.getId(), match.getId(), actor, "Assignment agent recommendation");
        } catch (Exception ex) {
            return ticket;
        }
    }

    public SpecialistProfile recommend(Ticket ticket) {
        SupportLevel required = requiredLevel(ticket);
        String skill = requiredSkill(ticket);
        List<SpecialistProfile> candidates = specialistProfileRepository.findAvailableByLevel(required);
        return candidates.stream()
                .filter(s -> skill == null || specialistSkillRepository.existsBySpecialistIdAndSkill_NameIgnoreCase(s.getId(), skill))
                .filter(s -> ticket.getCategory() == null || s.getTeam() == null || s.getTeam().getPrimaryCategory() == null
                        || s.getTeam().getPrimaryCategory().getId().equals(ticket.getCategory().getId()))
                .min(Comparator.comparingInt(SpecialistProfile::getCurrentWorkload))
                .orElse(candidates.stream().min(Comparator.comparingInt(SpecialistProfile::getCurrentWorkload)).orElse(null));
    }

    private SupportLevel requiredLevel(Ticket ticket) {
        if (ticket.getPriority() == Priority.CRITICAL) {
            return SupportLevel.L3;
        }
        if (ticket.getPriority() == Priority.HIGH) {
            return SupportLevel.L2;
        }
        return ticket.getCurrentSupportLevel() == null ? SupportLevel.L2 : ticket.getCurrentSupportLevel();
    }

    private String requiredSkill(Ticket ticket) {
        if (ticket.getSubcategory() != null && ticket.getSubcategory().getRequiredSkillName() != null) {
            return ticket.getSubcategory().getRequiredSkillName();
        }
        return ticket.getCategory() != null ? ticket.getCategory().getRequiredSkillName() : null;
    }
}
