package com.gridops.asset.service;

import com.gridops.asset.dto.AssetRequest;
import com.gridops.asset.dto.AssetResponse;
import com.gridops.asset.dto.AssetSummaryDto;
import com.gridops.asset.entity.Asset;
import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.entity.AssetType;
import com.gridops.asset.repository.AssetRepository;
import com.gridops.auth.service.UserService;
import com.gridops.integration.dto.TelemetryDto;
import com.gridops.integration.service.TelemetryAdapterService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserService userService;
    private final TelemetryAdapterService telemetryAdapterService;

    public AssetService(AssetRepository assetRepository, UserService userService,
                        TelemetryAdapterService telemetryAdapterService) {
        this.assetRepository = assetRepository;
        this.userService = userService;
        this.telemetryAdapterService = telemetryAdapterService;
    }

    @Transactional
    public AssetResponse create(AssetRequest request, Long userId) {
        if (request.getAssetTag() == null || request.getAssetTag().isBlank()) {
            throw new IllegalArgumentException("Asset tag is required");
        }
        if (request.getAssetType() == null) {
            throw new IllegalArgumentException("Asset type is required");
        }

        Asset asset = new Asset(
                request.getAssetTag().trim(),
                request.getName().trim(),
                request.getAssetType(),
                userId
        );

        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }
        asset.setLocation(request.getLocation());
        asset.setLatitude(request.getLatitude());
        asset.setLongitude(request.getLongitude());
        asset.setInstalledDate(request.getInstalledDate());

        asset = assetRepository.save(asset);

        String username = userService.findById(userId).getUsername();
        return AssetResponse.fromEntity(asset, username);
    }

    public AssetResponse findById(Long id) {
        Asset asset = loadAsset(id);
        String username = userService.findById(asset.getCreatedBy()).getUsername();
        return AssetResponse.fromEntity(asset, username);
    }

    public AssetSummaryDto findSummaryById(Long id) {
        Asset asset = loadAsset(id);
        return AssetSummaryDto.fromEntity(asset);
    }

    public Page<AssetResponse> findAll(Pageable pageable, AssetType type, AssetStatus status) {
        Page<Asset> page;

        if (type != null && status != null) {
            page = assetRepository.findByAssetTypeAndStatus(type, status, pageable);
        } else if (type != null) {
            page = assetRepository.findByAssetType(type, pageable);
        } else if (status != null) {
            page = assetRepository.findByStatus(status, pageable);
        } else {
            page = assetRepository.findAll(pageable);
        }

        Map<Long, String> usernameCache = page.getContent().stream()
                .map(Asset::getCreatedBy)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> userService.findById(id).getUsername()
                ));

        return page.map(asset -> AssetResponse.fromEntity(
                asset, usernameCache.get(asset.getCreatedBy())));
    }

    @Transactional
    public AssetResponse update(Long id, AssetRequest request) {
        Asset asset = loadAsset(id);

        asset.setName(request.getName().trim());
        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }
        asset.setLocation(request.getLocation());
        asset.setLatitude(request.getLatitude());
        asset.setLongitude(request.getLongitude());
        asset.setInstalledDate(request.getInstalledDate());

        asset = assetRepository.save(asset);

        String username = userService.findById(asset.getCreatedBy()).getUsername();
        return AssetResponse.fromEntity(asset, username);
    }

    @Transactional
    public AssetResponse deactivate(Long id) {
        Asset asset = loadAsset(id);
        asset.setStatus(AssetStatus.OFFLINE);
        asset = assetRepository.save(asset);

        String username = userService.findById(asset.getCreatedBy()).getUsername();
        return AssetResponse.fromEntity(asset, username);
    }

    public TelemetryDto getTelemetry(Long assetId) {
        Asset asset = loadAsset(assetId);
        return telemetryAdapterService.getTelemetry(asset.getAssetTag());
    }

    public long countByStatus(AssetStatus status) {
        return assetRepository.countByStatus(status);
    }

    private Asset loadAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found with id: " + id));
    }
}
