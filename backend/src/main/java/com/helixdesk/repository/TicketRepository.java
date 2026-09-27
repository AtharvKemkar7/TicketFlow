package com.helixdesk.repository;

import com.helixdesk.entity.Ticket;
import com.helixdesk.enums.Priority;
import com.helixdesk.enums.SlaState;
import com.helixdesk.enums.TicketStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") Long id);
    List<Ticket> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    List<Ticket> findByAssignedSpecialistIdOrderByCreatedAtDesc(Long specialistId);
    List<Ticket> findByAssignedSpecialistIdAndStatus(Long specialistId, TicketStatus status);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByPriority(Priority priority);
    List<Ticket> findBySlaState(SlaState slaState);
    long countByStatus(TicketStatus status);
    long countByAssignedSpecialistIdAndStatusNotIn(Long specialistId, List<TicketStatus> statuses);

    @Query("select count(t) from Ticket t")
    long totalTickets();
}
