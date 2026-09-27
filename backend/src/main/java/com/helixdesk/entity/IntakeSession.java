package com.helixdesk.entity;

import com.helixdesk.enums.Impact;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "intake_sessions")
public class IntakeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(columnDefinition = "TEXT")
    private String conversationJson;

    private String suggestedCategory;
    private String suggestedSubcategory;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Impact impact;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Urgency urgency;

    @Column(length = 400)
    private String businessEffect;

    @Column(length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String problemSummary;

    private boolean complete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_ticket_id")
    private Ticket createdTicket;

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

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public String getConversationJson() {
        return conversationJson;
    }

    public void setConversationJson(String conversationJson) {
        this.conversationJson = conversationJson;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public String getSuggestedSubcategory() {
        return suggestedSubcategory;
    }

    public void setSuggestedSubcategory(String suggestedSubcategory) {
        this.suggestedSubcategory = suggestedSubcategory;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProblemSummary() {
        return problemSummary;
    }

    public void setProblemSummary(String problemSummary) {
        this.problemSummary = problemSummary;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Ticket getCreatedTicket() {
        return createdTicket;
    }

    public void setCreatedTicket(Ticket createdTicket) {
        this.createdTicket = createdTicket;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
