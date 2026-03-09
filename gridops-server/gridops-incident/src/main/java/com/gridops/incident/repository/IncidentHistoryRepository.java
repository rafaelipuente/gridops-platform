package com.gridops.incident.repository;

import com.gridops.incident.entity.IncidentHistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {

    Page<IncidentHistory> findByIncidentIdOrderByCreatedAtDesc(Long incidentId, Pageable pageable);
}
