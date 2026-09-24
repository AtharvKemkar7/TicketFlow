package com.helixdesk.dto;

import com.helixdesk.entity.Ticket;
import com.helixdesk.enums.Impact;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.SlaState;
import com.helixdesk.enums.SupportLevel;
import com.helixdesk.enums.TicketStatus;
import com.helixdesk.enums.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class TicketDtos {
    private TicketDtos() {
    }

    public record CreateTicketRequest(
            @NotBlank String title,
            @NotBlank String description,
            Long categoryId,
            Long subcategoryId,
            @NotNull Impact impact,
            @NotNull Urgency urgency,
            String businessEffect
    ) {
    }

    public record TicketResponse(
            Long id,
            String ticketNumber,
            String title,
            String description,
            TicketStatus status,
            Priority priority,
            Impact impact,
            Urgency urgency,
            String businessEffect,
            Long requesterId,
            String requesterName,
            Long categoryId,
            String categoryName,
            Long subcategoryId,
            String subcategoryName,
            Long assignedSpecialistId,
            String assignedSpecialistName,
            Long assignedTeamId,
            String assignedTeamName,
            SupportLevel currentSupportLevel,
            SlaState slaState,
            LocalDateTime slaResponseDueAt,
            LocalDateTime slaResolutionDueAt,
            String resolutionSummary,
            int aiAttemptCount,
            boolean humanRequested,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static TicketResponse from(Ticket ticket) {
            return new TicketResponse(
                    ticket.getId(),
                    ticket.getTicketNumber(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getStatus(),
                    ticket.getPriority(),
                    ticket.getImpact(),
                    ticket.getUrgency(),
                    ticket.getBusinessEffect(),
                    ticket.getRequester() != null ? ticket.getRequester().getId() : null,
                    ticket.getRequester() != null ? ticket.getRequester().fullName() : null,
                    ticket.getCategory() != null ? ticket.getCategory().getId() : null,
                    ticket.getCategory() != null ? ticket.getCategory().getName() : null,
                    ticket.getSubcategory() != null ? ticket.getSubcategory().getId() : null,
                    ticket.getSubcategory() != null ? ticket.getSubcategory().getName() : null,
                    ticket.getAssignedSpecialist() != null ? ticket.getAssignedSpecialist().getId() : null,
                    ticket.getAssignedSpecialist() != null && ticket.getAssignedSpecialist().getUser() != null
                            ? ticket.getAssignedSpecialist().getUser().fullName() : null,
                    ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getId() : null,
                    ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : null,
                    ticket.getCurrentSupportLevel(),
                    ticket.getSlaState(),
                    ticket.getSlaResponseDueAt(),
                    ticket.getSlaResolutionDueAt(),
                    ticket.getResolutionSummary(),
                    ticket.getAiAttemptCount(),
                    ticket.isHumanRequested(),
                    ticket.getCreatedAt(),
                    ticket.getUpdatedAt()
            );
        }
    }

    public record AssignRequest(@NotNull Long specialistId, String reason) {
    }

    public record ReassignRequest(@NotNull Long specialistId, @NotBlank String reason) {
    }

    public record EscalateRequest(@NotNull Long specialistId, @NotBlank String reason, String notes) {
    }

    public record ResolveRequest(@NotBlank String resolutionSummary) {
    }

    public record ConfirmRequest(boolean accepted, String feedback) {
    }

    public record CommentRequest(@NotBlank String body, boolean internal) {
    }

    public record ChangeCategoryRequest(@NotNull Long categoryId, Long subcategoryId, String reason) {
    }

    public record ChangePriorityRequest(@NotNull Priority priority, String reason) {
    }
}
