package com.gridops.asset.controller;

import com.gridops.asset.dto.AssetRequest;
import com.gridops.asset.dto.AssetResponse;
import com.gridops.asset.dto.InspectionRequest;
import com.gridops.asset.dto.InspectionResponse;
import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.entity.AssetType;
import com.gridops.asset.service.AssetService;
import com.gridops.asset.service.InspectionService;
import com.gridops.auth.service.UserService;
import com.gridops.integration.dto.TelemetryDto;
import com.gridops.integration.service.TelemetryAdapterService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;
    private final InspectionService inspectionService;
    private final UserService userService;
    private final TelemetryAdapterService telemetryAdapterService;

    public AssetController(AssetService assetService,
                           InspectionService inspectionService,
                           UserService userService,
                           TelemetryAdapterService telemetryAdapterService) {
        this.assetService = assetService;
        this.inspectionService = inspectionService;
        this.userService = userService;
        this.telemetryAdapterService = telemetryAdapterService;
    }

    @GetMapping
    public ResponseEntity<Page<AssetResponse>> list(
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) AssetStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(assetService.findAll(pageable, type, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<AssetResponse> create(
            @Valid @RequestBody AssetRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<AssetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.ok(assetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssetResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.deactivate(id));
    }

    @GetMapping("/{id}/telemetry")
    public ResponseEntity<TelemetryDto> getTelemetry(@PathVariable Long id) {
        AssetResponse asset = assetService.findById(id);
        TelemetryDto telemetry = telemetryAdapterService.getTelemetry(asset.getAssetTag());
        return ResponseEntity.ok(telemetry);
    }

    @GetMapping("/{id}/inspections")
    public ResponseEntity<Page<InspectionResponse>> listInspections(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(inspectionService.findByAssetId(id, pageable));
    }

    @PostMapping("/{id}/inspections")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<InspectionResponse> createInspection(
            @PathVariable Long id,
            @Valid @RequestBody InspectionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inspectionService.create(id, request, userId));
    }
}
