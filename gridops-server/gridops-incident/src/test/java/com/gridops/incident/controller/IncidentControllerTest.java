package com.gridops.incident.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridops.auth.dto.UserSummaryDto;
import com.gridops.auth.entity.Role;
import com.gridops.auth.service.UserService;
import com.gridops.incident.dto.IncidentAssignRequest;
import com.gridops.incident.dto.IncidentCreateRequest;
import com.gridops.incident.dto.IncidentHistoryResponse;
import com.gridops.incident.dto.IncidentResponse;
import com.gridops.incident.dto.IncidentStatusRequest;
import com.gridops.incident.dto.IncidentUpdateRequest;
import com.gridops.incident.entity.IncidentSeverity;
import com.gridops.incident.entity.IncidentStatus;
import com.gridops.incident.service.IncidentHistoryService;
import com.gridops.incident.service.IncidentService;

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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
@Import({IncidentControllerTest.TestSecurityConfig.class, IncidentControllerTest.TestExceptionHandler.class})
class IncidentControllerTest {

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

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", 409, "error", ex.getMessage()));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IncidentService incidentService;

    @MockBean
    private IncidentHistoryService historyService;

    @MockBean
    private UserService userService;

    private static final Instant NOW = Instant.now();

    private IncidentResponse sampleIncidentResponse() {
        IncidentResponse r = new IncidentResponse();
        r.setId(1L);
        r.setIncidentNumber("INC-000001");
        r.setTitle("Power outage at St. Johns Substation");
        r.setDescription("Complete loss of power affecting north Portland service area");
        r.setSeverity(IncidentSeverity.HIGH);
        r.setStatus(IncidentStatus.OPEN);
        r.setAssetId(1L);
        r.setAssetTag("SUB-PDX-001");
        r.setReportedBy(4L);
        r.setReportedByUsername("rthompson");
        r.setCreatedAt(NOW);
        r.setUpdatedAt(NOW);
        return r;
    }

    private IncidentResponse assignedIncidentResponse() {
        IncidentResponse r = sampleIncidentResponse();
        r.setStatus(IncidentStatus.ASSIGNED);
        r.setAssignedTo(2L);
        r.setAssignedToUsername("jsmith");
        return r;
    }

    private IncidentHistoryResponse sampleHistoryResponse(String field, String oldVal, String newVal) {
        IncidentHistoryResponse h = new IncidentHistoryResponse();
        h.setId(1L);
        h.setIncidentId(1L);
        h.setChangedBy(1L);
        h.setChangedByUsername("kgarcia");
        h.setFieldChanged(field);
        h.setOldValue(oldVal);
        h.setNewValue(newVal);
        h.setCreatedAt(NOW);
        return h;
    }

    // --- GET /api/incidents ---

    @Test
    @WithMockUser
    void listIncidents_authenticated_returnsPage() throws Exception {
        when(incidentService.findAll(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(sampleIncidentResponse())));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].incidentNumber").value("INC-000001"))
                .andExpect(jsonPath("$.content[0].reportedByUsername").value("rthompson"))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    @WithMockUser
    void listIncidents_filteredByStatus_returnsPage() throws Exception {
        when(incidentService.findAll(any(Pageable.class), eq(IncidentStatus.OPEN)))
                .thenReturn(new PageImpl<>(List.of(sampleIncidentResponse())));

        mockMvc.perform(get("/api/incidents").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    void listIncidents_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /api/incidents/{id} ---

    @Test
    @WithMockUser
    void getIncident_exists_returnsIncident() throws Exception {
        when(incidentService.findById(1L)).thenReturn(sampleIncidentResponse());

        mockMvc.perform(get("/api/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.incidentNumber").value("INC-000001"))
                .andExpect(jsonPath("$.title").value("Power outage at St. Johns Substation"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.assetTag").value("SUB-PDX-001"));
    }

    @Test
    @WithMockUser
    void getIncident_notFound_returns404() throws Exception {
        when(incidentService.findById(99L))
                .thenThrow(new EntityNotFoundException("Incident not found with id: 99"));

        mockMvc.perform(get("/api/incidents/99"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/incidents ---

    @Test
    @WithMockUser(username = "rthompson", roles = "OPERATOR")
    void createIncident_asOperator_returns201() throws Exception {
        when(userService.findByUsername("rthompson"))
                .thenReturn(new UserSummaryDto(4L, "rthompson", Role.OPERATOR));
        when(incidentService.create(any(IncidentCreateRequest.class), eq(4L)))
                .thenReturn(sampleIncidentResponse());

        IncidentCreateRequest request = new IncidentCreateRequest();
        request.setTitle("Power outage at St. Johns Substation");
        request.setDescription("Complete loss of power affecting north Portland service area");
        request.setSeverity(IncidentSeverity.HIGH);
        request.setAssetId(1L);

        mockMvc.perform(post("/api/incidents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.incidentNumber").value("INC-000001"))
                .andExpect(jsonPath("$.reportedByUsername").value("rthompson"));
    }

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void createIncident_asAdmin_returns201() throws Exception {
        when(userService.findByUsername("kgarcia"))
                .thenReturn(new UserSummaryDto(1L, "kgarcia", Role.ADMIN));
        when(incidentService.create(any(IncidentCreateRequest.class), eq(1L)))
                .thenReturn(sampleIncidentResponse());

        IncidentCreateRequest request = new IncidentCreateRequest();
        request.setTitle("Power outage at St. Johns Substation");
        request.setSeverity(IncidentSeverity.HIGH);

        mockMvc.perform(post("/api/incidents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void createIncident_invalidRequest_returns400() throws Exception {
        IncidentCreateRequest request = new IncidentCreateRequest();
        // title and severity are both missing — violates @NotBlank / @NotNull

        mockMvc.perform(post("/api/incidents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /api/incidents/{id} ---

    @Test
    @WithMockUser(username = "jsmith", roles = "ENGINEER")
    void updateIncident_asEngineer_returns200() throws Exception {
        IncidentResponse updated = sampleIncidentResponse();
        updated.setTitle("Updated title");
        updated.setSeverity(IncidentSeverity.CRITICAL);

        when(userService.findByUsername("jsmith"))
                .thenReturn(new UserSummaryDto(2L, "jsmith", Role.ENGINEER));
        when(incidentService.update(eq(1L), any(IncidentUpdateRequest.class), eq(2L)))
                .thenReturn(updated);

        IncidentUpdateRequest request = new IncidentUpdateRequest();
        request.setTitle("Updated title");
        request.setDescription("Updated description");
        request.setSeverity(IncidentSeverity.CRITICAL);

        mockMvc.perform(put("/api/incidents/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"));
    }

    @Test
    @WithMockUser(username = "rthompson", roles = "OPERATOR")
    void updateIncident_asOperator_returns403() throws Exception {
        IncidentUpdateRequest request = new IncidentUpdateRequest();
        request.setTitle("Should not work");
        request.setSeverity(IncidentSeverity.LOW);

        mockMvc.perform(put("/api/incidents/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- PATCH /api/incidents/{id}/assign ---

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void assignIncident_asAdmin_returns200() throws Exception {
        when(userService.findByUsername("kgarcia"))
                .thenReturn(new UserSummaryDto(1L, "kgarcia", Role.ADMIN));
        when(incidentService.assign(eq(1L), any(IncidentAssignRequest.class), eq(1L)))
                .thenReturn(assignedIncidentResponse());

        IncidentAssignRequest request = new IncidentAssignRequest();
        request.setAssignedToUserId(2L);

        mockMvc.perform(patch("/api/incidents/1/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedToUsername").value("jsmith"));
    }

    @Test
    @WithMockUser(username = "jsmith", roles = "ENGINEER")
    void assignIncident_asEngineer_returns403() throws Exception {
        IncidentAssignRequest request = new IncidentAssignRequest();
        request.setAssignedToUserId(2L);

        mockMvc.perform(patch("/api/incidents/1/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "rthompson", roles = "OPERATOR")
    void assignIncident_asOperator_returns403() throws Exception {
        IncidentAssignRequest request = new IncidentAssignRequest();
        request.setAssignedToUserId(2L);

        mockMvc.perform(patch("/api/incidents/1/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- PATCH /api/incidents/{id}/status ---

    @Test
    @WithMockUser(username = "jsmith", roles = "ENGINEER")
    void transitionStatus_validTransition_returns200() throws Exception {
        IncidentResponse resolved = sampleIncidentResponse();
        resolved.setStatus(IncidentStatus.RESOLVED);
        resolved.setResolutionNotes("Replaced fuse and restored power");
        resolved.setResolvedAt(NOW);

        when(userService.findByUsername("jsmith"))
                .thenReturn(new UserSummaryDto(2L, "jsmith", Role.ENGINEER));
        when(incidentService.transitionStatus(eq(1L), any(IncidentStatusRequest.class), eq(2L)))
                .thenReturn(resolved);

        IncidentStatusRequest request = new IncidentStatusRequest();
        request.setStatus(IncidentStatus.RESOLVED);
        request.setResolutionNotes("Replaced fuse and restored power");

        mockMvc.perform(patch("/api/incidents/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionNotes").value("Replaced fuse and restored power"));
    }

    @Test
    @WithMockUser(username = "jsmith", roles = "ENGINEER")
    void transitionStatus_invalidTransition_returns409() throws Exception {
        when(userService.findByUsername("jsmith"))
                .thenReturn(new UserSummaryDto(2L, "jsmith", Role.ENGINEER));
        when(incidentService.transitionStatus(eq(1L), any(IncidentStatusRequest.class), eq(2L)))
                .thenThrow(new IllegalStateException("Cannot transition from OPEN to RESOLVED"));

        IncidentStatusRequest request = new IncidentStatusRequest();
        request.setStatus(IncidentStatus.RESOLVED);
        request.setResolutionNotes("Attempting invalid skip");

        mockMvc.perform(patch("/api/incidents/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot transition from OPEN to RESOLVED"));
    }

    @Test
    @WithMockUser(username = "rthompson", roles = "OPERATOR")
    void transitionStatus_asOperator_returns403() throws Exception {
        IncidentStatusRequest request = new IncidentStatusRequest();
        request.setStatus(IncidentStatus.CLOSED);

        mockMvc.perform(patch("/api/incidents/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/incidents/{id}/history ---

    @Test
    @WithMockUser
    void getHistory_returnsAuditTrail() throws Exception {
        when(historyService.findByIncidentId(1L)).thenReturn(List.of(
                sampleHistoryResponse("status", null, "OPEN"),
                sampleHistoryResponse("assigned_to", null, "jsmith"),
                sampleHistoryResponse("status", "OPEN", "ASSIGNED")
        ));

        mockMvc.perform(get("/api/incidents/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].fieldChanged").value("status"))
                .andExpect(jsonPath("$[0].newValue").value("OPEN"))
                .andExpect(jsonPath("$[0].changedByUsername").value("kgarcia"))
                .andExpect(jsonPath("$[1].fieldChanged").value("assigned_to"))
                .andExpect(jsonPath("$[2].newValue").value("ASSIGNED"));
    }

    // --- 404 on mutating endpoints ---

    @Test
    @WithMockUser(username = "kgarcia", roles = "ADMIN")
    void assignIncident_notFound_returns404() throws Exception {
        when(userService.findByUsername("kgarcia"))
                .thenReturn(new UserSummaryDto(1L, "kgarcia", Role.ADMIN));
        when(incidentService.assign(eq(99L), any(IncidentAssignRequest.class), eq(1L)))
                .thenThrow(new EntityNotFoundException("Incident not found with id: 99"));

        IncidentAssignRequest request = new IncidentAssignRequest();
        request.setAssignedToUserId(2L);

        mockMvc.perform(patch("/api/incidents/99/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "jsmith", roles = "ENGINEER")
    void updateIncident_notFound_returns404() throws Exception {
        when(userService.findByUsername("jsmith"))
                .thenReturn(new UserSummaryDto(2L, "jsmith", Role.ENGINEER));
        when(incidentService.update(eq(99L), any(IncidentUpdateRequest.class), eq(2L)))
                .thenThrow(new EntityNotFoundException("Incident not found with id: 99"));

        IncidentUpdateRequest request = new IncidentUpdateRequest();
        request.setTitle("Does not matter");
        request.setSeverity(IncidentSeverity.LOW);

        mockMvc.perform(put("/api/incidents/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
