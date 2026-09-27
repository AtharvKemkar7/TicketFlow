package com.helixdesk.service;

import com.helixdesk.enums.TicketStatus;
import com.helixdesk.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class TicketWorkflowService {

    private final Map<TicketStatus, Set<TicketStatus>> allowed = new EnumMap<>(TicketStatus.class);

    public TicketWorkflowService() {
        allowed.put(TicketStatus.NEW, EnumSet.of(
                TicketStatus.AI_PROCESSING, TicketStatus.HUMAN_REQUIRED, TicketStatus.ASSIGNED, TicketStatus.CANCELLED));
        allowed.put(TicketStatus.AI_PROCESSING, EnumSet.of(
                TicketStatus.AI_ATTEMPTING_RESOLUTION, TicketStatus.HUMAN_REQUIRED, TicketStatus.CANCELLED));
        allowed.put(TicketStatus.AI_ATTEMPTING_RESOLUTION, EnumSet.of(
                TicketStatus.AI_ATTEMPTING_RESOLUTION, TicketStatus.USER_CONFIRMATION, TicketStatus.RESOLVED,
                TicketStatus.HUMAN_REQUIRED, TicketStatus.WAITING_FOR_USER, TicketStatus.CANCELLED));
        allowed.put(TicketStatus.HUMAN_REQUIRED, EnumSet.of(
                TicketStatus.ASSIGNED, TicketStatus.CANCELLED));
        allowed.put(TicketStatus.ASSIGNED, EnumSet.of(
                TicketStatus.IN_PROGRESS, TicketStatus.REASSIGNED, TicketStatus.ESCALATED,
                TicketStatus.WAITING_FOR_USER, TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        allowed.put(TicketStatus.IN_PROGRESS, EnumSet.of(
                TicketStatus.WAITING_FOR_USER, TicketStatus.RESOLVED, TicketStatus.REASSIGNED,
                TicketStatus.ESCALATED, TicketStatus.ASSIGNED));
        allowed.put(TicketStatus.WAITING_FOR_USER, EnumSet.of(
                TicketStatus.IN_PROGRESS, TicketStatus.ASSIGNED, TicketStatus.RESOLVED,
                TicketStatus.AI_ATTEMPTING_RESOLUTION, TicketStatus.CANCELLED));
        allowed.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.USER_CONFIRMATION, TicketStatus.CLOSED, TicketStatus.REOPENED));
        allowed.put(TicketStatus.USER_CONFIRMATION, EnumSet.of(
                TicketStatus.CLOSED, TicketStatus.REOPENED));
        allowed.put(TicketStatus.REOPENED, EnumSet.of(
                TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.HUMAN_REQUIRED, TicketStatus.AI_PROCESSING));
        allowed.put(TicketStatus.REASSIGNED, EnumSet.of(
                TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS));
        allowed.put(TicketStatus.ESCALATED, EnumSet.of(
                TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.HUMAN_REQUIRED));
        allowed.put(TicketStatus.CLOSED, EnumSet.of(TicketStatus.REOPENED));
        allowed.put(TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));
    }

    public void assertTransition(TicketStatus from, TicketStatus to) {
        Set<TicketStatus> next = allowed.getOrDefault(from, EnumSet.noneOf(TicketStatus.class));
        if (!next.contains(to)) {
            throw new BadRequestException("Invalid status transition: " + from + " -> " + to);
        }
    }
}
