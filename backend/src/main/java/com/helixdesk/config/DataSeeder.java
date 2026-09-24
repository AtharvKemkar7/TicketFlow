package com.helixdesk.config;

import com.helixdesk.entity.Category;
import com.helixdesk.entity.KnowledgeArticle;
import com.helixdesk.entity.PriorityRule;
import com.helixdesk.entity.Skill;
import com.helixdesk.entity.SlaPolicy;
import com.helixdesk.entity.SpecialistProfile;
import com.helixdesk.entity.SpecialistSkill;
import com.helixdesk.entity.Subcategory;
import com.helixdesk.entity.SystemSetting;
import com.helixdesk.entity.Team;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AccountStatus;
import com.helixdesk.enums.ArticleStatus;
import com.helixdesk.enums.AvailabilityStatus;
import com.helixdesk.enums.Impact;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.RoleType;
import com.helixdesk.enums.SupportLevel;
import com.helixdesk.enums.Urgency;
import com.helixdesk.repository.CategoryRepository;
import com.helixdesk.repository.KnowledgeArticleRepository;
import com.helixdesk.repository.PriorityRuleRepository;
import com.helixdesk.repository.SkillRepository;
import com.helixdesk.repository.SlaPolicyRepository;
import com.helixdesk.repository.SpecialistProfileRepository;
import com.helixdesk.repository.SpecialistSkillRepository;
import com.helixdesk.repository.SubcategoryRepository;
import com.helixdesk.repository.SystemSettingRepository;
import com.helixdesk.repository.TeamRepository;
import com.helixdesk.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            UserAccountRepository users,
            PasswordEncoder encoder,
            CategoryRepository categories,
            SubcategoryRepository subcategories,
            TeamRepository teams,
            SkillRepository skills,
            SpecialistProfileRepository specialists,
            SpecialistSkillRepository specialistSkills,
            PriorityRuleRepository priorityRules,
            SlaPolicyRepository slaPolicies,
            KnowledgeArticleRepository articles,
            SystemSettingRepository settings
    ) {
        return args -> {
            if (users.count() > 0) {
                return;
            }

            UserAccount admin = user(users, encoder, "admin@helixdesk.local", "Admin", "Helix", RoleType.ADMIN, "IT Operations");
            UserAccount specialistUser = user(users, encoder, "specialist@helixdesk.local", "Avery", "Chen", RoleType.SPECIALIST, "Network");
            UserAccount specialistPeer = user(users, encoder, "l2@helixdesk.local", "Morgan", "Lee", RoleType.SPECIALIST, "Network");
            UserAccount specialistTwo = user(users, encoder, "l3@helixdesk.local", "Jordan", "Patel", RoleType.SPECIALIST, "Network");
            user(users, encoder, "user@helixdesk.local", "Sam", "Rivera", RoleType.USER, "Finance");

            Category network = category(categories, "NETWORK", "Network connectivity and remote access", "VPN");
            Category access = category(categories, "ACCESS", "Identity and access management", "IAM");
            Category hardware = category(categories, "HARDWARE", "Endpoints and peripherals", "HARDWARE");
            Category software = category(categories, "SOFTWARE", "Business applications", "APPLICATION");
            Category email = category(categories, "EMAIL", "Messaging platforms", "EMAIL");

            subcategory(subcategories, "VPN", network, "VPN");
            subcategory(subcategories, "CONNECTIVITY", network, "NETWORK");
            subcategory(subcategories, "CREDENTIALS", access, "IAM");
            subcategory(subcategories, "DEVICE", hardware, "HARDWARE");
            subcategory(subcategories, "APPLICATION", software, "APPLICATION");
            subcategory(subcategories, "OUTLOOK", email, "EMAIL");

            Team netTeam = team(teams, "Network Operations", network);
            team(teams, "Endpoint Support", hardware);
            team(teams, "Identity Services", access);

            Skill vpn = skill(skills, "VPN");
            Skill iam = skill(skills, "IAM");
            skill(skills, "HARDWARE");
            skill(skills, "APPLICATION");
            skill(skills, "EMAIL");
            skill(skills, "NETWORK");

            SpecialistProfile l2 = specialist(specialists, specialistUser, netTeam, SupportLevel.L2);
            SpecialistProfile l2b = specialist(specialists, specialistPeer, netTeam, SupportLevel.L2);
            SpecialistProfile l3 = specialist(specialists, specialistTwo, netTeam, SupportLevel.L3);
            linkSkill(specialistSkills, l2, vpn);
            linkSkill(specialistSkills, l2b, vpn);
            linkSkill(specialistSkills, l3, vpn);

            for (Impact impact : Impact.values()) {
                for (Urgency urgency : Urgency.values()) {
                    PriorityRule rule = new PriorityRule();
                    rule.setImpact(impact);
                    rule.setUrgency(urgency);
                    rule.setPriority(mapPriority(impact, urgency));
                    priorityRules.save(rule);
                }
            }

            sla(slaPolicies, Priority.LOW, 240, 1440, 120);
            sla(slaPolicies, Priority.MEDIUM, 120, 480, 60);
            sla(slaPolicies, Priority.HIGH, 30, 240, 30);
            sla(slaPolicies, Priority.CRITICAL, 15, 120, 20);

            KnowledgeArticle vpnArticle = new KnowledgeArticle();
            vpnArticle.setTitle("VPN Connection Troubleshooting");
            vpnArticle.setTags("vpn,network,remote");
            vpnArticle.setStatus(ArticleStatus.PUBLISHED);
            vpnArticle.setCategory(network);
            vpnArticle.setCreatedBy(admin);
            vpnArticle.setUpdatedBy(admin);
            vpnArticle.setContent("""
                    Attempt 1: Restart the VPN client, confirm you are on a trusted network, and retry the corporate VPN profile.
                    Disconnect any personal VPN, then reconnect using the company client.
                    Attempt 2: Verify VPN credentials, check DNS, confirm the VPN gateway is reachable, and review local firewall rules.
                    If the client still fails, capture the error code and request a specialist.
                    """);
            articles.save(vpnArticle);

            KnowledgeArticle accessArticle = new KnowledgeArticle();
            accessArticle.setTitle("Account Lockout Recovery");
            accessArticle.setTags("password,iam,login");
            accessArticle.setStatus(ArticleStatus.PUBLISHED);
            accessArticle.setCategory(access);
            accessArticle.setCreatedBy(admin);
            accessArticle.setUpdatedBy(admin);
            accessArticle.setContent("""
                    Attempt 1: Use the self-service password reset portal and wait five minutes before signing in again.
                    Attempt 2: Confirm MFA device time sync and retry. If still locked, a specialist must unlock the directory account.
                    """);
            articles.save(accessArticle);

            SystemSetting setting = new SystemSetting();
            setting.setSettingKey("ticket.prefix");
            setting.setSettingValue("HX");
            settings.save(setting);
        };
    }

    private UserAccount user(UserAccountRepository repo, PasswordEncoder encoder, String email, String first, String last, RoleType role, String dept) {
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode("Password123!"));
        user.setFirstName(first);
        user.setLastName(last);
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);
        user.setDepartment(dept);
        return repo.save(user);
    }

    private Category category(CategoryRepository repo, String name, String description, String skill) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setRequiredSkillName(skill);
        category.setActive(true);
        return repo.save(category);
    }

    private void subcategory(SubcategoryRepository repo, String name, Category category, String skill) {
        Subcategory subcategory = new Subcategory();
        subcategory.setName(name);
        subcategory.setCategory(category);
        subcategory.setRequiredSkillName(skill);
        subcategory.setActive(true);
        repo.save(subcategory);
    }

    private Team team(TeamRepository repo, String name, Category category) {
        Team team = new Team();
        team.setName(name);
        team.setDescription(name);
        team.setActive(true);
        team.setPrimaryCategory(category);
        return repo.save(team);
    }

    private Skill skill(SkillRepository repo, String name) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setActive(true);
        return repo.save(skill);
    }

    private SpecialistProfile specialist(SpecialistProfileRepository repo, UserAccount user, Team team, SupportLevel level) {
        SpecialistProfile profile = new SpecialistProfile();
        profile.setUser(user);
        profile.setTeam(team);
        profile.setSupportLevel(level);
        profile.setAvailability(AvailabilityStatus.AVAILABLE);
        profile.setActive(true);
        return repo.save(profile);
    }

    private void linkSkill(SpecialistSkillRepository repo, SpecialistProfile profile, Skill skill) {
        SpecialistSkill link = new SpecialistSkill();
        link.setSpecialist(profile);
        link.setSkill(skill);
        repo.save(link);
    }

    private void sla(SlaPolicyRepository repo, Priority priority, int response, int resolution, int atRisk) {
        SlaPolicy policy = new SlaPolicy();
        policy.setPriority(priority);
        policy.setResponseTargetMinutes(response);
        policy.setResolutionTargetMinutes(resolution);
        policy.setAtRiskThresholdMinutes(atRisk);
        repo.save(policy);
    }

    private Priority mapPriority(Impact impact, Urgency urgency) {
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
