package com.helixdesk.entity;

import com.helixdesk.enums.Impact;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.SlaState;
import com.helixdesk.enums.SupportLevel;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.enums.Urgency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String ticketNumber;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketStatus status = TicketStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Impact impact = Impact.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency = Urgency.MEDIUM;

    @Column(length = 400)
    private String businessEffect;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id")
    private UserAccount requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_specialist_id")
    private SpecialistProfile assignedSpecialist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private SupportLevel currentSupportLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlaState slaState = SlaState.ON_TRACK;

    private LocalDateTime slaResponseDueAt;
    private LocalDateTime slaResolutionDueAt;
    private LocalDateTime firstRespondedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    @Column(columnDefinition = "TEXT")
    private String resolutionSummary;

    private int aiAttemptCount = 0;
    private boolean humanRequested = false;

    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Impact getImpact() {
        return impact;
    }

    public void setImpact(Impact impact) {
        this.impact = impact;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public String getBusinessEffect() {
        return businessEffect;
    }

    public void setBusinessEffect(String businessEffect) {
        this.businessEffect = businessEffect;
    }

    public UserAccount getRequester() {
        return requester;
    }

    public void setRequester(UserAccount requester) {
        this.requester = requester;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    public SpecialistProfile getAssignedSpecialist() {
        return assignedSpecialist;
    }

    public void setAssignedSpecialist(SpecialistProfile assignedSpecialist) {
        this.assignedSpecialist = assignedSpecialist;
    }

    public Team getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(Team assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public SupportLevel getCurrentSupportLevel() {
        return currentSupportLevel;
    }

    public void setCurrentSupportLevel(SupportLevel currentSupportLevel) {
        this.currentSupportLevel = currentSupportLevel;
    }

    public SlaState getSlaState() {
        return slaState;
    }

    public void setSlaState(SlaState slaState) {
        this.slaState = slaState;
    }

    public LocalDateTime getSlaResponseDueAt() {
        return slaResponseDueAt;
    }

    public void setSlaResponseDueAt(LocalDateTime slaResponseDueAt) {
        this.slaResponseDueAt = slaResponseDueAt;
    }

    public LocalDateTime getSlaResolutionDueAt() {
        return slaResolutionDueAt;
    }

    public void setSlaResolutionDueAt(LocalDateTime slaResolutionDueAt) {
        this.slaResolutionDueAt = slaResolutionDueAt;
    }

    public LocalDateTime getFirstRespondedAt() {
        return firstRespondedAt;
    }

    public void setFirstRespondedAt(LocalDateTime firstRespondedAt) {
        this.firstRespondedAt = firstRespondedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    public int getAiAttemptCount() {
        return aiAttemptCount;
    }

    public void setAiAttemptCount(int aiAttemptCount) {
        this.aiAttemptCount = aiAttemptCount;
    }

    public boolean isHumanRequested() {
        return humanRequested;
    }

    public void setHumanRequested(boolean humanRequested) {
        this.humanRequested = humanRequested;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
