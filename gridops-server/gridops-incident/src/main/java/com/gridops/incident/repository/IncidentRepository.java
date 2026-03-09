package com.gridops.incident.repository;

import com.gridops.incident.entity.Incident;
import com.gridops.incident.entity.IncidentSeverity;
import com.gridops.incident.entity.IncidentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByIncidentNumber(String incidentNumber);

    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);

    Page<Incident> findByAssignedTo(Long assignedTo, Pageable pageable);

    Page<Incident> findByAssetId(Long assetId, Pageable pageable);

    long countByStatus(IncidentStatus status);

    long countBySeverity(IncidentSeverity severity);

    @Query(value = "SELECT nextval('incident_number_seq')", nativeQuery = true)
    Long getNextSequenceValue();
}
