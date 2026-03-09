package com.gridops.incident.service;

import com.gridops.asset.dto.AssetSummaryDto;
import com.gridops.asset.service.AssetService;
import com.gridops.auth.dto.UserSummaryDto;
import com.gridops.auth.entity.Role;
import com.gridops.auth.service.UserService;
import com.gridops.incident.dto.IncidentAssignRequest;
import com.gridops.incident.dto.IncidentCreateRequest;
import com.gridops.incident.dto.IncidentResponse;
import com.gridops.incident.dto.IncidentStatusRequest;
import com.gridops.incident.dto.IncidentUpdateRequest;
import com.gridops.incident.entity.Incident;
import com.gridops.incident.entity.IncidentSeverity;
import com.gridops.incident.entity.IncidentStatus;
import com.gridops.incident.repository.IncidentRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentHistoryService historyService;
    private final UserService userService;
    private final AssetService assetService;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentHistoryService historyService,
                           UserService userService,
                           AssetService assetService) {
        this.incidentRepository = incidentRepository;
        this.historyService = historyService;
        this.userService = userService;
        this.assetService = assetService;
    }

    @Transactional
    public IncidentResponse create(IncidentCreateRequest request, Long reporterId) {
        if (request.getAssetId() != null) {
            assetService.findSummaryById(request.getAssetId());
        }

        Long seq = incidentRepository.getNextSequenceValue();
        String incidentNumber = String.format("INC-%06d", seq);

        Incident incident = new Incident(
                incidentNumber,
                request.getTitle().trim(),
                request.getSeverity(),
                reporterId
        );
        incident.setDescription(request.getDescription());
        incident.setAssetId(request.getAssetId());

        incident = incidentRepository.save(incident);

        historyService.record(incident.getId(), reporterId, "status",
                null, IncidentStatus.OPEN.name(), null);

        return toResponse(incident);
    }

    public IncidentResponse findById(Long id) {
        return toResponse(loadIncident(id));
    }

    public Page<IncidentResponse> findAll(Pageable pageable, IncidentStatus status) {
        Page<Incident> page;
        if (status != null) {
            page = incidentRepository.findByStatus(status, pageable);
        } else {
            page = incidentRepository.findAll(pageable);
        }

        Set<Long> userIds = new HashSet<>();
        page.getContent().forEach(inc -> {
            userIds.add(inc.getReportedBy());
            if (inc.getAssignedTo() != null) {
                userIds.add(inc.getAssignedTo());
            }
        });
        Map<Long, String> usernameCache = userIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> userService.findById(id).getUsername()));

        Set<Long> assetIds = page.getContent().stream()
                .map(Incident::getAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> assetTagCache = assetIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> assetService.findSummaryById(id).getAssetTag()));

        return page.map(inc -> toResponse(inc, usernameCache, assetTagCache));
    }

    @Transactional
    public IncidentResponse update(Long id, IncidentUpdateRequest request, Long userId) {
        Incident incident = loadIncident(id);

        String newTitle = request.getTitle().trim();
        if (!incident.getTitle().equals(newTitle)) {
            historyService.record(incident.getId(), userId, "title",
                    incident.getTitle(), newTitle, null);
        }
        if (incident.getSeverity() != request.getSeverity()) {
            historyService.record(incident.getId(), userId, "severity",
                    incident.getSeverity().name(), request.getSeverity().name(), null);
        }

        incident.setTitle(newTitle);
        incident.setDescription(request.getDescription());
        incident.setSeverity(request.getSeverity());

        incident = incidentRepository.save(incident);
        return toResponse(incident);
    }

    @Transactional
    public IncidentResponse assign(Long id, IncidentAssignRequest request, Long userId) {
        Incident incident = loadIncident(id);

        UserSummaryDto assignee = userService.findById(request.getAssignedToUserId());
        if (assignee.getRole() != Role.ENGINEER) {
            throw new IllegalArgumentException("Incidents can only be assigned to engineers");
        }

        IncidentStatus currentStatus = incident.getStatus();
        if (currentStatus == IncidentStatus.OPEN) {
            currentStatus.validateTransition(IncidentStatus.ASSIGNED);
            historyService.record(incident.getId(), userId, "status",
                    currentStatus.name(), IncidentStatus.ASSIGNED.name(), null);
            incident.setStatus(IncidentStatus.ASSIGNED);
        } else if (currentStatus != IncidentStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Cannot reassign incident in " + currentStatus + " status");
        }

        String oldAssigneeUsername = incident.getAssignedTo() != null
                ? userService.findById(incident.getAssignedTo()).getUsername()
                : null;
        historyService.record(incident.getId(), userId, "assigned_to",
                oldAssigneeUsername, assignee.getUsername(), null);

        incident.setAssignedTo(request.getAssignedToUserId());
        incident = incidentRepository.save(incident);
        return toResponse(incident);
    }

    @Transactional
    public IncidentResponse transitionStatus(Long id, IncidentStatusRequest request, Long userId) {
        Incident incident = loadIncident(id);
        IncidentStatus oldStatus = incident.getStatus();
        IncidentStatus newStatus = request.getStatus();

        oldStatus.validateTransition(newStatus);

        if (newStatus == IncidentStatus.RESOLVED) {
            if (request.getResolutionNotes() == null || request.getResolutionNotes().isBlank()) {
                throw new IllegalArgumentException(
                        "Resolution notes are required when resolving an incident");
            }
            incident.setResolutionNotes(request.getResolutionNotes());
            incident.setResolvedAt(Instant.now());
        }

        if (newStatus == IncidentStatus.CLOSED) {
            incident.setClosedAt(Instant.now());
        }

        incident.setStatus(newStatus);
        incident = incidentRepository.save(incident);

        historyService.record(incident.getId(), userId, "status",
                oldStatus.name(), newStatus.name(), request.getResolutionNotes());

        return toResponse(incident);
    }

    public long countByStatus(IncidentStatus status) {
        return incidentRepository.countByStatus(status);
    }

    public long countBySeverity(IncidentSeverity severity) {
        return incidentRepository.countBySeverity(severity);
    }

    private Incident loadIncident(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found with id: " + id));
    }

    private IncidentResponse toResponse(Incident incident) {
        Map<Long, String> usernameCache = new HashMap<>();
        usernameCache.put(incident.getReportedBy(),
                userService.findById(incident.getReportedBy()).getUsername());
        if (incident.getAssignedTo() != null) {
            usernameCache.put(incident.getAssignedTo(),
                    userService.findById(incident.getAssignedTo()).getUsername());
        }

        Map<Long, String> assetTagCache = new HashMap<>();
        if (incident.getAssetId() != null) {
            assetTagCache.put(incident.getAssetId(),
                    assetService.findSummaryById(incident.getAssetId()).getAssetTag());
        }

        return toResponse(incident, usernameCache, assetTagCache);
    }

    private IncidentResponse toResponse(Incident incident,
                                        Map<Long, String> usernameCache,
                                        Map<Long, String> assetTagCache) {
        IncidentResponse r = new IncidentResponse();
        r.setId(incident.getId());
        r.setIncidentNumber(incident.getIncidentNumber());
        r.setTitle(incident.getTitle());
        r.setDescription(incident.getDescription());
        r.setSeverity(incident.getSeverity());
        r.setStatus(incident.getStatus());
        r.setAssetId(incident.getAssetId());
        r.setAssetTag(incident.getAssetId() != null
                ? assetTagCache.get(incident.getAssetId()) : null);
        r.setReportedBy(incident.getReportedBy());
        r.setReportedByUsername(usernameCache.get(incident.getReportedBy()));
        r.setAssignedTo(incident.getAssignedTo());
        r.setAssignedToUsername(incident.getAssignedTo() != null
                ? usernameCache.get(incident.getAssignedTo()) : null);
        r.setResolutionNotes(incident.getResolutionNotes());
        r.setCreatedAt(incident.getCreatedAt());
        r.setUpdatedAt(incident.getUpdatedAt());
        r.setResolvedAt(incident.getResolvedAt());
        r.setClosedAt(incident.getClosedAt());
        return r;
    }
}
