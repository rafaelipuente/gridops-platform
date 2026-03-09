package com.gridops.asset.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridops.asset.dto.AssetRequest;
import com.gridops.asset.dto.AssetResponse;
import com.gridops.asset.dto.InspectionRequest;
import com.gridops.asset.dto.InspectionResponse;
import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.entity.AssetType;
import com.gridops.asset.entity.InspectionCondition;
import com.gridops.asset.service.AssetService;
import com.gridops.asset.service.InspectionService;
import com.gridops.auth.dto.UserSummaryDto;
import com.gridops.auth.entity.Role;
import com.gridops.auth.service.UserService;
import com.gridops.integration.dto.TelemetryDto;
import com.gridops.integration.service.TelemetryAdapterService;
import com.gridops.integration.service.TelemetryAdapterService.TelemetryUnavailableException;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
@Import({AssetControllerTest.TestSecurityConfig.class, AssetControllerTest.TestExceptionHandler.class})
class AssetControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    @ControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", ex.getMessage()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "error", ex.getMessage()));
        }

        @ExceptionHandler(TelemetryUnavailableException.class)
        public ResponseEntity<Map<String, Object>> handleTelemetryUnavailable(
                TelemetryUnavailableException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", 503, "error", ex.getMessage()));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    @MockBean
    private InspectionService inspectionService;

    @MockBean
    private UserService userService;

    private static final Instant NOW = Instant.now();

    private AssetResponse sampleAssetResponse() {
        return new AssetResponse(
                1L, "SUB-PDX-001", "St. Johns Substation", AssetType.SUBSTATION,
                AssetStatus.OPERATIONAL, "8500 N Bradford St", new BigDecimal("45.5907000"),
                new BigDecimal("-122.7530000"), LocalDate.of(2008, 6, 14),
                1L, "kgarcia", NOW, NOW);
    }

    private InspectionResponse sampleInspectionResponse() {
        return new InspectionResponse(
                1L, 1L, 2L, "jsmith", NOW, "Annual inspection complete",
                InspectionCondition.GOOD, NOW);
    }

    // --- GET /api/assets ---

    @Test
    @WithMockUser
    void listAssets_authenticated_returnsPage() throws Exception {
        when(assetService.findAll(any(Pageable.class), any(), any()))
                .thenReturn(new PageImpl<>(List.of(sampleAssetResponse())));

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assetTag").value("SUB-PDX-001"))
                .andExpect(jsonPath("$.content[0].createdByUsername").value("kgarcia"));
    }

    @Test
    void listAssets_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /api/assets/{id} ---

    @Test
    @WithMockUser
    void getAssetDetail_exists_returnsAsset() throws Exception {
        when(assetService.findById(1L)).thenReturn(sampleAssetResponse());

        mockMvc.perform(get("/api/assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.assetTag").value("SUB-PDX-001"))
                .andExpect(jsonPath("$.name").value("St. Johns Substation"))
                .andExpect(jsonPath("$.assetType").value("SUBSTATION"))
                .andExpect(jsonPath("$.status").value("OPERATIONAL"));
    }

    @Test
    @WithMockUser
    void getAssetDetail_notFound_returns404() throws Exception {
        when(assetService.findById(99L))
                .thenThrow(new EntityNotFoundException("Asset not found with id: 99"));

        mockMvc.perform(get("/api/assets/99"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/assets ---

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void createAsset_asAdmin_returns201() throws Exception {
        when(userService.findByUsername("kgarcia"))
                .thenReturn(new UserSummaryDto(1L, "kgarcia", Role.ADMIN));
        when(assetService.create(any(AssetRequest.class), eq(1L)))
                .thenReturn(sampleAssetResponse());

        AssetRequest request = new AssetRequest();
        request.setAssetTag("SUB-PDX-001");
        request.setName("St. Johns Substation");
        request.setAssetType(AssetType.SUBSTATION);

        mockMvc.perform(post("/api/assets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetTag").value("SUB-PDX-001"));
    }

    @Test
    @WithMockUser(username = "jsmith", roles = "ENGINEER")
    void createAsset_asEngineer_returns201() throws Exception {
        when(userService.findByUsername("jsmith"))
                .thenReturn(new UserSummaryDto(2L, "jsmith", Role.ENGINEER));
        when(assetService.create(any(AssetRequest.class), eq(2L)))
                .thenReturn(sampleAssetResponse());

        AssetRequest request = new AssetRequest();
        request.setAssetTag("SUB-PDX-001");
        request.setName("St. Johns Substation");
        request.setAssetType(AssetType.SUBSTATION);

        mockMvc.perform(post("/api/assets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "rthompson", roles = "OPERATOR")
    void createAsset_asOperator_returns403() throws Exception {
        AssetRequest request = new AssetRequest();
        request.setAssetTag("SUB-PDX-003");
        request.setName("Test Substation");
        request.setAssetType(AssetType.SUBSTATION);

        mockMvc.perform(post("/api/assets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void createAsset_invalidRequest_returns400() throws Exception {
        AssetRequest request = new AssetRequest();
        // name is blank — violates @NotBlank

        mockMvc.perform(post("/api/assets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /api/assets/{id} ---

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void updateAsset_asAdmin_returns200() throws Exception {
        AssetResponse updated = new AssetResponse(
                1L, "SUB-PDX-001", "Updated Name", AssetType.SUBSTATION,
                AssetStatus.DEGRADED, "New Location", new BigDecimal("45.5907000"),
                new BigDecimal("-122.7530000"), LocalDate.of(2008, 6, 14),
                1L, "kgarcia", NOW, NOW);
        when(assetService.update(eq(1L), any(AssetRequest.class))).thenReturn(updated);

        AssetRequest request = new AssetRequest();
        request.setName("Updated Name");
        request.setStatus(AssetStatus.DEGRADED);
        request.setLocation("New Location");

        mockMvc.perform(put("/api/assets/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.status").value("DEGRADED"));
    }

    // --- DELETE /api/assets/{id} ---

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void deleteAsset_asAdmin_returns200() throws Exception {
        AssetResponse deactivated = new AssetResponse(
                1L, "SUB-PDX-001", "St. Johns Substation", AssetType.SUBSTATION,
                AssetStatus.OFFLINE, "8500 N Bradford St", new BigDecimal("45.5907000"),
                new BigDecimal("-122.7530000"), LocalDate.of(2008, 6, 14),
                1L, "kgarcia", NOW, NOW);
        when(assetService.deactivate(1L)).thenReturn(deactivated);

        mockMvc.perform(delete("/api/assets/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OFFLINE"));
    }

    @Test
    @WithMockUser(username = "jsmith", roles = "ENGINEER")
    void deleteAsset_asEngineer_returns403() throws Exception {
        mockMvc.perform(delete("/api/assets/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/assets/{id}/inspections ---

    @Test
    @WithMockUser
    void listInspections_authenticated_returnsPage() throws Exception {
        when(inspectionService.findByAssetId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleInspectionResponse())));

        mockMvc.perform(get("/api/assets/1/inspections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].inspectedByUsername").value("jsmith"))
                .andExpect(jsonPath("$.content[0].condition").value("GOOD"));
    }

    // --- POST /api/assets/{id}/inspections ---

    @Test
    @WithMockUser(username = "jsmith", roles = "ENGINEER")
    void createInspection_asEngineer_returns201() throws Exception {
        when(userService.findByUsername("jsmith"))
                .thenReturn(new UserSummaryDto(2L, "jsmith", Role.ENGINEER));
        when(inspectionService.create(eq(1L), any(InspectionRequest.class), eq(2L)))
                .thenReturn(sampleInspectionResponse());

        InspectionRequest request = new InspectionRequest();
        request.setInspectionDate(NOW);
        request.setCondition(InspectionCondition.GOOD);
        request.setNotes("Annual inspection");

        mockMvc.perform(post("/api/assets/1/inspections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inspectedByUsername").value("jsmith"));
    }

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void createInspection_asAdmin_returns403() throws Exception {
        InspectionRequest request = new InspectionRequest();
        request.setInspectionDate(NOW);
        request.setCondition(InspectionCondition.GOOD);

        mockMvc.perform(post("/api/assets/1/inspections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/assets/{id}/telemetry ---

    @Test
    @WithMockUser
    void getTelemetry_returnsReading() throws Exception {
        when(assetService.getTelemetry(1L))
                .thenReturn(new TelemetryDto("SUB-PDX-001", NOW,
                        new BigDecimal("65.3"), new BigDecimal("72.1"),
                        new BigDecimal("121.50"), new BigDecimal("45.20"), "NORMAL"));

        mockMvc.perform(get("/api/assets/1/telemetry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetTag").value("SUB-PDX-001"))
                .andExpect(jsonPath("$.temperatureCelsius").value(65.3))
                .andExpect(jsonPath("$.status").value("NORMAL"));
    }

    @Test
    @WithMockUser
    void getTelemetry_serviceDown_returns503() throws Exception {
        when(assetService.getTelemetry(1L))
                .thenThrow(new TelemetryUnavailableException(
                        "Telemetry service is currently unavailable", new RuntimeException()));

        mockMvc.perform(get("/api/assets/1/telemetry"))
                .andExpect(status().isServiceUnavailable());
    }
}
