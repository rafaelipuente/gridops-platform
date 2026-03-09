package com.gridops.incident.service;

import com.gridops.incident.dto.IncidentHistoryResponse;
import com.gridops.incident.entity.IncidentHistory;
import com.gridops.incident.repository.IncidentHistoryRepository;
import com.gridops.auth.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class IncidentHistoryService {

    private final IncidentHistoryRepository historyRepository;
    private final UserService userService;

    public IncidentHistoryService(IncidentHistoryRepository historyRepository,
                                  UserService userService) {
        this.historyRepository = historyRepository;
        this.userService = userService;
    }

    @Transactional
    public void record(Long incidentId, Long changedBy, String field,
                       String oldValue, String newValue, String note) {
        IncidentHistory history = new IncidentHistory(
                incidentId, changedBy, field, oldValue, newValue, note);
        historyRepository.save(history);
    }

    public List<IncidentHistoryResponse> findByIncidentId(Long incidentId) {
        Page<IncidentHistory> page = historyRepository
                .findByIncidentIdOrderByCreatedAtDesc(incidentId, Pageable.unpaged());

        Map<Long, String> usernameCache = page.getContent().stream()
                .map(IncidentHistory::getChangedBy)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> userService.findById(id).getUsername()
                ));

        return page.getContent().stream()
                .map(h -> toResponse(h, usernameCache.get(h.getChangedBy())))
                .toList();
    }

    private IncidentHistoryResponse toResponse(IncidentHistory h, String changedByUsername) {
        IncidentHistoryResponse r = new IncidentHistoryResponse();
        r.setId(h.getId());
        r.setIncidentId(h.getIncidentId());
        r.setChangedBy(h.getChangedBy());
        r.setChangedByUsername(changedByUsername);
        r.setFieldChanged(h.getFieldChanged());
        r.setOldValue(h.getOldValue());
        r.setNewValue(h.getNewValue());
        r.setChangeNote(h.getChangeNote());
        r.setCreatedAt(h.getCreatedAt());
        return r;
    }
}
