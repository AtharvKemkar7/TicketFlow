package com.helixdesk.service;

import com.helixdesk.dto.CatalogDtos;
import com.helixdesk.entity.Skill;
import com.helixdesk.entity.SpecialistProfile;
import com.helixdesk.entity.SpecialistSkill;
import com.helixdesk.entity.Team;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.RoleType;
import com.helixdesk.enums.SupportLevel;
import com.helixdesk.exception.BadRequestException;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.SkillRepository;
import com.helixdesk.repository.SpecialistProfileRepository;
import com.helixdesk.repository.SpecialistSkillRepository;
import com.helixdesk.repository.TeamRepository;
import com.helixdesk.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpecialistAdminService {

    private final SpecialistProfileRepository specialistProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final TeamRepository teamRepository;
    private final SkillRepository skillRepository;
    private final SpecialistSkillRepository specialistSkillRepository;

    public SpecialistAdminService(
            SpecialistProfileRepository specialistProfileRepository,
            UserAccountRepository userAccountRepository,
            TeamRepository teamRepository,
            SkillRepository skillRepository,
            SpecialistSkillRepository specialistSkillRepository
    ) {
        this.specialistProfileRepository = specialistProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.teamRepository = teamRepository;
        this.skillRepository = skillRepository;
        this.specialistSkillRepository = specialistSkillRepository;
    }

    public List<SpecialistProfile> all() {
        return specialistProfileRepository.findAll();
    }

    public SpecialistProfile get(Long id) {
        return specialistProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("Specialist not found"));
    }

    public SpecialistProfile byUser(Long userId) {
        return specialistProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Specialist profile not found"));
    }

    @Transactional
    public SpecialistProfile save(Long id, CatalogDtos.SpecialistRequest request) {
        SpecialistProfile profile = id == null ? new SpecialistProfile() : get(id);
        UserAccount user = userAccountRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() != RoleType.SPECIALIST && user.getRole() != RoleType.ADMIN) {
            user.setRole(RoleType.SPECIALIST);
        }
        profile.setUser(user);
        if (request.teamId() != null) {
            Team team = teamRepository.findById(request.teamId()).orElseThrow(() -> new NotFoundException("Team not found"));
            profile.setTeam(team);
        }
        profile.setSupportLevel(request.supportLevel() == null ? SupportLevel.L1 : request.supportLevel());
        if (request.availability() != null) {
            profile.setAvailability(request.availability());
        }
        profile.setActive(request.active());
        specialistProfileRepository.save(profile);
        if (request.skillIds() != null) {
            List<SpecialistSkill> existing = specialistSkillRepository.findBySpecialistId(profile.getId());
            specialistSkillRepository.deleteAll(existing);
            for (Long skillId : request.skillIds()) {
                Skill skill = skillRepository.findById(skillId).orElseThrow(() -> new BadRequestException("Skill not found"));
                SpecialistSkill link = new SpecialistSkill();
                link.setSpecialist(profile);
                link.setSkill(skill);
                specialistSkillRepository.save(link);
            }
        }
        return profile;
    }

    @Transactional
    public SpecialistProfile updateAvailability(Long userId, com.helixdesk.enums.AvailabilityStatus availability) {
        SpecialistProfile profile = byUser(userId);
        profile.setAvailability(availability);
        return profile;
    }
}
