package com.helixdesk.repository;

import com.helixdesk.entity.SlaPolicy;
import com.helixdesk.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    Optional<SlaPolicy> findByPriority(Priority priority);
}
