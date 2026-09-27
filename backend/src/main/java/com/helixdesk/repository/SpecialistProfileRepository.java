package com.helixdesk.repository;

import com.helixdesk.entity.SpecialistProfile;
import com.helixdesk.enums.AvailabilityStatus;
import com.helixdesk.enums.SupportLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpecialistProfileRepository extends JpaRepository<SpecialistProfile, Long> {
    Optional<SpecialistProfile> findByUserId(Long userId);

    List<SpecialistProfile> findByActiveTrueAndAvailability(AvailabilityStatus availability);

    @Query("""
            select s from SpecialistProfile s
            where s.active = true
              and s.availability = com.helixdesk.enums.AvailabilityStatus.AVAILABLE
              and s.supportLevel = :level
            order by s.currentWorkload asc
            """)
    List<SpecialistProfile> findAvailableByLevel(@Param("level") SupportLevel level);
}
