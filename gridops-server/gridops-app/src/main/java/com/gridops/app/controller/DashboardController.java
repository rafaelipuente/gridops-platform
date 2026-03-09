package com.gridops.app.controller;

import com.gridops.app.dto.DashboardResponse;
import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.service.AssetService;
import com.gridops.incident.entity.IncidentSeverity;
import com.gridops.incident.entity.IncidentStatus;
import com.gridops.incident.service.IncidentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AssetService assetService;
    private final IncidentService incidentService;

    public DashboardController(AssetService assetService, IncidentService incidentService) {
        this.assetService = assetService;
        this.incidentService = incidentService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> summary() {
        long totalAssets = assetService.countByStatus(AssetStatus.OPERATIONAL)
                + assetService.countByStatus(AssetStatus.DEGRADED)
                + assetService.countByStatus(AssetStatus.OFFLINE)
                + assetService.countByStatus(AssetStatus.MAINTENANCE);
        long assetsOffline = assetService.countByStatus(AssetStatus.OFFLINE);
        long openIncidents = incidentService.countByStatus(IncidentStatus.OPEN);
        long criticalIncidents = incidentService.countBySeverity(IncidentSeverity.CRITICAL);

        return ResponseEntity.ok(new DashboardResponse(
                totalAssets, assetsOffline, openIncidents, criticalIncidents));
    }
}
