package com.helixdesk.dto;

import com.helixdesk.enums.Impact;
import com.helixdesk.enums.Urgency;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class AiDtos {
    private AiDtos() {
    }

    public record IntakeMessageRequest(Long sessionId, @NotBlank String message) {
    }

    public record IntakeMessageResponse(
            Long sessionId,
            String reply,
            boolean complete,
            String suggestedCategory,
            String suggestedSubcategory,
            Impact impact,
            Urgency urgency,
            String title,
            Long createdTicketId,
            String ticketNumber
    ) {
    }

    public record ResolutionMessageRequest(@NotBlank String message) {
    }

    public record ResolutionMessageResponse(
            String reply,
            int attemptCount,
            boolean handedOff,
            String handoffReason,
            List<String> sources
    ) {
    }
}
