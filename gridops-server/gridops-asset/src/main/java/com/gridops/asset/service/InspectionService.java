package com.gridops.asset.service;

import com.gridops.asset.dto.InspectionRequest;
import com.gridops.asset.dto.InspectionResponse;
import com.gridops.asset.entity.AssetInspection;
import com.gridops.asset.repository.AssetRepository;
import com.gridops.asset.repository.InspectionRepository;
import com.gridops.auth.service.UserService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final AssetRepository assetRepository;
    private final UserService userService;

    public InspectionService(InspectionRepository inspectionRepository,
                             AssetRepository assetRepository,
                             UserService userService) {
        this.inspectionRepository = inspectionRepository;
        this.assetRepository = assetRepository;
        this.userService = userService;
    }

    @Transactional
    public InspectionResponse create(Long assetId, InspectionRequest request, Long userId) {
        if (!assetRepository.existsById(assetId)) {
            throw new EntityNotFoundException("Asset not found with id: " + assetId);
        }

        AssetInspection inspection = new AssetInspection(
                assetId,
                userId,
                request.getInspectionDate(),
                request.getNotes(),
                request.getCondition()
        );

        inspection = inspectionRepository.save(inspection);

        String username = userService.findById(userId).getUsername();
        return InspectionResponse.fromEntity(inspection, username);
    }

    public Page<InspectionResponse> findByAssetId(Long assetId, Pageable pageable) {
        if (!assetRepository.existsById(assetId)) {
            throw new EntityNotFoundException("Asset not found with id: " + assetId);
        }

        Page<AssetInspection> page = inspectionRepository
                .findByAssetIdOrderByInspectionDateDesc(assetId, pageable);

        Map<Long, String> usernameCache = page.getContent().stream()
                .map(AssetInspection::getInspectedBy)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> userService.findById(id).getUsername()
                ));

        return page.map(inspection -> InspectionResponse.fromEntity(
                inspection, usernameCache.get(inspection.getInspectedBy())));
    }
}
