package com.gridops.asset.service;

import com.gridops.asset.dto.AssetRequest;
import com.gridops.asset.dto.AssetResponse;
import com.gridops.asset.dto.AssetSummaryDto;
import com.gridops.asset.entity.Asset;
import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.entity.AssetType;
import com.gridops.asset.repository.AssetRepository;
import com.gridops.auth.dto.UserSummaryDto;
import com.gridops.auth.entity.Role;
import com.gridops.auth.service.UserService;
import com.gridops.integration.service.TelemetryAdapterService;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserService userService;

    @Mock
    private TelemetryAdapterService telemetryAdapterService;

    @InjectMocks
    private AssetService assetService;

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "kgarcia";
    private UserSummaryDto userSummary;

    @BeforeEach
    void setUp() {
        userSummary = new UserSummaryDto(USER_ID, USERNAME, Role.ADMIN);
    }

    @Test
    void create_withValidRequest_succeeds() {
        AssetRequest request = new AssetRequest();
        request.setAssetTag("SUB-TEST-001");
        request.setName("Test Substation");
        request.setAssetType(AssetType.SUBSTATION);
        request.setLocation("123 Main St, Portland, OR");
        request.setLatitude(new BigDecimal("45.5907000"));
        request.setLongitude(new BigDecimal("-122.7530000"));
        request.setInstalledDate(LocalDate.of(2020, 1, 15));

        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> {
            Asset saved = invocation.getArgument(0);
            setField(saved, "id", 1L);
            setField(saved, "createdAt", Instant.now());
            setField(saved, "updatedAt", Instant.now());
            return saved;
        });
        when(userService.findById(USER_ID)).thenReturn(userSummary);

        AssetResponse response = assetService.create(request, USER_ID);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("SUB-TEST-001", response.getAssetTag());
        assertEquals("Test Substation", response.getName());
        assertEquals(AssetType.SUBSTATION, response.getAssetType());
        assertEquals(AssetStatus.OPERATIONAL, response.getStatus());
        assertEquals("123 Main St, Portland, OR", response.getLocation());
        assertEquals(new BigDecimal("45.5907000"), response.getLatitude());
        assertEquals(new BigDecimal("-122.7530000"), response.getLongitude());
        assertEquals(LocalDate.of(2020, 1, 15), response.getInstalledDate());
        assertEquals(USER_ID, response.getCreatedBy());
        assertEquals(USERNAME, response.getCreatedByUsername());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        verify(assetRepository).save(any(Asset.class));
        verify(userService).findById(USER_ID);
    }

    @Test
    void create_withDuplicateTag_propagatesConstraintViolation() {
        AssetRequest request = new AssetRequest();
        request.setAssetTag("SUB-PDX-001");
        request.setName("Duplicate Substation");
        request.setAssetType(AssetType.SUBSTATION);

        when(assetRepository.save(any(Asset.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThrows(DataIntegrityViolationException.class,
                () -> assetService.create(request, USER_ID));
    }

    @Test
    void create_withMissingAssetTag_throwsIllegalArgument() {
        AssetRequest request = new AssetRequest();
        request.setName("No Tag");
        request.setAssetType(AssetType.SUBSTATION);

        assertThrows(IllegalArgumentException.class,
                () -> assetService.create(request, USER_ID));

        verify(assetRepository, never()).save(any());
    }

    @Test
    void create_withMissingAssetType_throwsIllegalArgument() {
        AssetRequest request = new AssetRequest();
        request.setAssetTag("SUB-TEST-001");
        request.setName("No Type");

        assertThrows(IllegalArgumentException.class,
                () -> assetService.create(request, USER_ID));

        verify(assetRepository, never()).save(any());
    }

    @Test
    void findById_returnsMappedResponse() {
        Asset asset = buildAsset(1L, "SUB-PDX-001", "St. Johns Substation",
                AssetType.SUBSTATION, USER_ID);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(userService.findById(USER_ID)).thenReturn(userSummary);

        AssetResponse response = assetService.findById(1L);

        assertEquals(1L, response.getId());
        assertEquals("SUB-PDX-001", response.getAssetTag());
        assertEquals("St. Johns Substation", response.getName());
        assertEquals(AssetType.SUBSTATION, response.getAssetType());
        assertEquals(AssetStatus.OPERATIONAL, response.getStatus());
        assertEquals(USERNAME, response.getCreatedByUsername());
    }

    @Test
    void findById_notFound_throwsEntityNotFound() {
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> assetService.findById(99L));
    }

    @Test
    void findSummaryById_returnsCrossModuleDto() {
        Asset asset = buildAsset(1L, "SUB-PDX-001", "St. Johns Substation",
                AssetType.SUBSTATION, USER_ID);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        AssetSummaryDto summary = assetService.findSummaryById(1L);

        assertEquals(1L, summary.getId());
        assertEquals("SUB-PDX-001", summary.getAssetTag());
        assertEquals("St. Johns Substation", summary.getName());
        assertEquals(AssetType.SUBSTATION, summary.getAssetType());
        assertEquals(AssetStatus.OPERATIONAL, summary.getStatus());

        verifyNoInteractions(userService);
    }

    @Test
    void update_changesOnlyMutableFields() {
        Asset existing = buildAsset(1L, "SUB-PDX-001", "Old Name",
                AssetType.SUBSTATION, USER_ID);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));
        when(userService.findById(USER_ID)).thenReturn(userSummary);

        AssetRequest request = new AssetRequest();
        request.setName("New Name");
        request.setStatus(AssetStatus.DEGRADED);
        request.setLocation("456 Updated Ave");
        request.setLatitude(new BigDecimal("45.4632000"));
        request.setLongitude(new BigDecimal("-122.6554000"));
        request.setInstalledDate(LocalDate.of(2022, 6, 1));

        AssetResponse response = assetService.update(1L, request);

        assertEquals("New Name", response.getName());
        assertEquals(AssetStatus.DEGRADED, response.getStatus());
        assertEquals("456 Updated Ave", response.getLocation());
        assertEquals(new BigDecimal("45.4632000"), response.getLatitude());
        assertEquals("SUB-PDX-001", response.getAssetTag());
        assertEquals(AssetType.SUBSTATION, response.getAssetType());
    }

    @Test
    void update_withNullStatus_preservesExistingStatus() {
        Asset existing = buildAsset(1L, "SUB-PDX-001", "Old Name",
                AssetType.SUBSTATION, USER_ID);
        existing.setStatus(AssetStatus.DEGRADED);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));
        when(userService.findById(USER_ID)).thenReturn(userSummary);

        AssetRequest request = new AssetRequest();
        request.setName("Same Name");

        AssetResponse response = assetService.update(1L, request);

        assertEquals(AssetStatus.DEGRADED, response.getStatus());
    }

    @Test
    void deactivate_setsStatusToOffline() {
        Asset asset = buildAsset(1L, "SUB-PDX-001", "St. Johns Substation",
                AssetType.SUBSTATION, USER_ID);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));
        when(userService.findById(USER_ID)).thenReturn(userSummary);

        AssetResponse response = assetService.deactivate(1L);

        assertEquals(AssetStatus.OFFLINE, response.getStatus());
        verify(assetRepository).save(argThat(a -> a.getStatus() == AssetStatus.OFFLINE));
    }

    @Test
    void countByStatus_delegatesToRepository() {
        when(assetRepository.countByStatus(AssetStatus.OPERATIONAL)).thenReturn(7L);

        long count = assetService.countByStatus(AssetStatus.OPERATIONAL);

        assertEquals(7L, count);
        verify(assetRepository).countByStatus(AssetStatus.OPERATIONAL);
    }

    private Asset buildAsset(Long id, String assetTag, String name,
                             AssetType type, Long createdBy) {
        Asset asset = new Asset(assetTag, name, type, createdBy);
        setField(asset, "id", id);
        setField(asset, "createdAt", Instant.now());
        setField(asset, "updatedAt", Instant.now());
        return asset;
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
