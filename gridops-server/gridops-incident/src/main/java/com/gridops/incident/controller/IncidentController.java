package com.gridops.incident.controller;

import com.gridops.auth.service.UserService;
import com.gridops.incident.dto.IncidentAssignRequest;
import com.gridops.incident.dto.IncidentCreateRequest;
import com.gridops.incident.dto.IncidentHistoryResponse;
import com.gridops.incident.dto.IncidentResponse;
import com.gridops.incident.dto.IncidentStatusRequest;
import com.gridops.incident.dto.IncidentUpdateRequest;
import com.gridops.incident.entity.IncidentStatus;
import com.gridops.incident.service.IncidentHistoryService;
import com.gridops.incident.service.IncidentService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentHistoryService historyService;
    private final UserService userService;

    public IncidentController(IncidentService incidentService,
                              IncidentHistoryService historyService,
                              UserService userService) {
        this.incidentService = incidentService;
        this.historyService = historyService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> list(
            @RequestParam(required = false) IncidentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(incidentService.findAll(pageable, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> create(
            @Valid @RequestBody IncidentCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(incidentService.create(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<IncidentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(incidentService.update(id, request, userId));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentResponse> assign(
            @PathVariable Long id,
            @Valid @RequestBody IncidentAssignRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(incidentService.assign(id, request, userId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<IncidentResponse> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody IncidentStatusRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(incidentService.transitionStatus(id, request, userId));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<IncidentHistoryResponse>> history(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.findByIncidentId(id));
    }

    private Long resolveUserId(UserDetails principal) {
        return userService.findByUsername(principal.getUsername()).getId();
    }
}
