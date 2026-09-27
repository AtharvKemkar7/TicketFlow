package com.helixdesk.entity;

import com.helixdesk.enums.SupportLevel;
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
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_escalation_history")
public class TicketEscalationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SupportLevel fromLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SupportLevel toLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_specialist_id")
    private SpecialistProfile fromSpecialist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_specialist_id")
    private SpecialistProfile toSpecialist;

    @Column(nullable = false, length = 400)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public SupportLevel getFromLevel() {
        return fromLevel;
    }

    public void setFromLevel(SupportLevel fromLevel) {
        this.fromLevel = fromLevel;
    }

    public SupportLevel getToLevel() {
        return toLevel;
    }

    public void setToLevel(SupportLevel toLevel) {
        this.toLevel = toLevel;
    }

    public SpecialistProfile getFromSpecialist() {
        return fromSpecialist;
    }

    public void setFromSpecialist(SpecialistProfile fromSpecialist) {
        this.fromSpecialist = fromSpecialist;
    }

    public SpecialistProfile getToSpecialist() {
        return toSpecialist;
    }

    public void setToSpecialist(SpecialistProfile toSpecialist) {
        this.toSpecialist = toSpecialist;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
