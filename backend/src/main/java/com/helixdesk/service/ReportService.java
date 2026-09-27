package com.helixdesk.service;

import com.helixdesk.entity.Ticket;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.repository.AiResolutionAttemptRepository;
import com.helixdesk.repository.TicketEscalationHistoryRepository;
import com.helixdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TicketRepository ticketRepository;
    private final AiResolutionAttemptRepository attemptRepository;
    private final TicketEscalationHistoryRepository escalationHistoryRepository;

    public ReportService(
            TicketRepository ticketRepository,
            AiResolutionAttemptRepository attemptRepository,
            TicketEscalationHistoryRepository escalationHistoryRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.attemptRepository = attemptRepository;
        this.escalationHistoryRepository = escalationHistoryRepository;
    }

    public Map<String, Object> dashboard() {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalTickets", tickets.size());
        report.put("openTickets", tickets.stream().filter(t -> t.getStatus() != TicketStatus.CLOSED && t.getStatus() != TicketStatus.CANCELLED).count());
        report.put("closedTickets", tickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count());
        report.put("resolvedTickets", tickets.stream().filter(t -> t.getResolvedAt() != null).count());
        report.put("reopenedTickets", tickets.stream().filter(t -> t.getStatus() == TicketStatus.REOPENED).count());
        report.put("escalatedTickets", escalationHistoryRepository.count());
        report.put("byCategory", tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory() == null ? "UNCATEGORIZED" : t.getCategory().getName(), Collectors.counting())));
        report.put("byPriority", tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting())));
        report.put("byTeam", tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getAssignedTeam() == null ? "UNASSIGNED" : t.getAssignedTeam().getName(), Collectors.counting())));
        report.put("bySpecialist", tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getAssignedSpecialist() == null ? "UNASSIGNED" : t.getAssignedSpecialist().getUser().fullName(), Collectors.counting())));
        double avgHours = tickets.stream()
                .filter(t -> t.getResolvedAt() != null && t.getCreatedAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes())
                .average()
                .orElse(0) / 60.0;
        report.put("averageResolutionHours", Math.round(avgHours * 10.0) / 10.0);
        long slaOk = tickets.stream().filter(t -> t.getSlaState() != com.helixdesk.enums.SlaState.BREACHED).count();
        report.put("slaCompliancePercent", tickets.isEmpty() ? 100 : Math.round(slaOk * 100.0 / tickets.size()));
        report.put("aiResolutionAttempts", attemptRepository.count());
        report.put("aiToHumanHandoffs", attemptRepository.countByHandedOffTrue());
        return report;
    }
}
