package com.gridops.asset.service;

import com.gridops.asset.dto.InspectionRequest;
import com.gridops.asset.dto.InspectionResponse;
import com.gridops.asset.entity.AssetInspection;
import com.gridops.asset.entity.InspectionCondition;
import com.gridops.asset.repository.AssetRepository;
import com.gridops.asset.repository.InspectionRepository;
import com.gridops.auth.dto.UserSummaryDto;
import com.gridops.auth.entity.Role;
import com.gridops.auth.service.UserService;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private InspectionService inspectionService;

    private static final Long ASSET_ID = 1L;
    private static final Long INSPECTOR_ID = 2L;
    private static final String INSPECTOR_USERNAME = "jsmith";

    @Test
    void create_forExistingAsset_succeeds() {
        InspectionRequest request = new InspectionRequest();
        request.setInspectionDate(Instant.parse("2026-01-15T09:00:00Z"));
        request.setCondition(InspectionCondition.GOOD);
        request.setNotes("Annual relay test. All readings within tolerance.");

        when(assetRepository.existsById(ASSET_ID)).thenReturn(true);
        when(inspectionRepository.save(any(AssetInspection.class))).thenAnswer(invocation -> {
            AssetInspection saved = invocation.getArgument(0);
            setField(saved, "id", 1L);
            setField(saved, "createdAt", Instant.now());
            return saved;
        });
        when(userService.findById(INSPECTOR_ID))
                .thenReturn(new UserSummaryDto(INSPECTOR_ID, INSPECTOR_USERNAME, Role.ENGINEER));

        InspectionResponse response = inspectionService.create(ASSET_ID, request, INSPECTOR_ID);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(ASSET_ID, response.getAssetId());
        assertEquals(INSPECTOR_ID, response.getInspectedBy());
        assertEquals(INSPECTOR_USERNAME, response.getInspectedByUsername());
        assertEquals(InspectionCondition.GOOD, response.getCondition());
        assertEquals("Annual relay test. All readings within tolerance.", response.getNotes());
        assertNotNull(response.getCreatedAt());

        verify(assetRepository).existsById(ASSET_ID);
        verify(inspectionRepository).save(any(AssetInspection.class));
    }

    @Test
    void create_forMissingAsset_throwsEntityNotFound() {
        when(assetRepository.existsById(99L)).thenReturn(false);

        InspectionRequest request = new InspectionRequest();
        request.setInspectionDate(Instant.now());
        request.setCondition(InspectionCondition.FAIR);

        assertThrows(EntityNotFoundException.class,
                () -> inspectionService.create(99L, request, INSPECTOR_ID));

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void findByAssetId_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        AssetInspection i1 = buildInspection(1L, ASSET_ID, INSPECTOR_ID, InspectionCondition.GOOD);
        AssetInspection i2 = buildInspection(2L, ASSET_ID, INSPECTOR_ID, InspectionCondition.FAIR);
        Page<AssetInspection> page = new PageImpl<>(List.of(i1, i2), pageable, 2);

        when(assetRepository.existsById(ASSET_ID)).thenReturn(true);
        when(inspectionRepository.findByAssetIdOrderByInspectionDateDesc(ASSET_ID, pageable))
                .thenReturn(page);
        when(userService.findById(INSPECTOR_ID))
                .thenReturn(new UserSummaryDto(INSPECTOR_ID, INSPECTOR_USERNAME, Role.ENGINEER));

        Page<InspectionResponse> result = inspectionService.findByAssetId(ASSET_ID, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(INSPECTOR_USERNAME, result.getContent().get(0).getInspectedByUsername());
        assertEquals(INSPECTOR_USERNAME, result.getContent().get(1).getInspectedByUsername());

        verify(userService, times(1)).findById(INSPECTOR_ID);
    }

    @Test
    void findByAssetId_missingAsset_throwsEntityNotFound() {
        when(assetRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> inspectionService.findByAssetId(99L, PageRequest.of(0, 10)));

        verify(inspectionRepository, never()).findByAssetIdOrderByInspectionDateDesc(any(), any());
    }

    @Test
    void findByAssetId_multipleInspectors_resolvesEachOnce() {
        Long secondInspectorId = 3L;
        String secondInspectorName = "mjones";
        Pageable pageable = PageRequest.of(0, 10);

        AssetInspection i1 = buildInspection(1L, ASSET_ID, INSPECTOR_ID, InspectionCondition.GOOD);
        AssetInspection i2 = buildInspection(2L, ASSET_ID, secondInspectorId, InspectionCondition.POOR);
        AssetInspection i3 = buildInspection(3L, ASSET_ID, INSPECTOR_ID, InspectionCondition.FAIR);
        Page<AssetInspection> page = new PageImpl<>(List.of(i1, i2, i3), pageable, 3);

        when(assetRepository.existsById(ASSET_ID)).thenReturn(true);
        when(inspectionRepository.findByAssetIdOrderByInspectionDateDesc(ASSET_ID, pageable))
                .thenReturn(page);
        when(userService.findById(INSPECTOR_ID))
                .thenReturn(new UserSummaryDto(INSPECTOR_ID, INSPECTOR_USERNAME, Role.ENGINEER));
        when(userService.findById(secondInspectorId))
                .thenReturn(new UserSummaryDto(secondInspectorId, secondInspectorName, Role.ENGINEER));

        Page<InspectionResponse> result = inspectionService.findByAssetId(ASSET_ID, pageable);

        assertEquals(3, result.getTotalElements());
        assertEquals(INSPECTOR_USERNAME, result.getContent().get(0).getInspectedByUsername());
        assertEquals(secondInspectorName, result.getContent().get(1).getInspectedByUsername());
        assertEquals(INSPECTOR_USERNAME, result.getContent().get(2).getInspectedByUsername());

        verify(userService, times(1)).findById(INSPECTOR_ID);
        verify(userService, times(1)).findById(secondInspectorId);
    }

    private AssetInspection buildInspection(Long id, Long assetId, Long inspectedBy,
                                            InspectionCondition condition) {
        AssetInspection inspection = new AssetInspection(
                assetId, inspectedBy, Instant.now(), "Test notes", condition);
        setField(inspection, "id", id);
        setField(inspection, "createdAt", Instant.now());
        return inspection;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
