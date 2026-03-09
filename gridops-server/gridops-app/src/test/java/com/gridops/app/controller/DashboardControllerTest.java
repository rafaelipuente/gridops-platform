package com.gridops.app.controller;

import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.service.AssetService;
import com.gridops.incident.entity.IncidentSeverity;
import com.gridops.incident.entity.IncidentStatus;
import com.gridops.incident.service.IncidentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetService assetService;

    @MockBean
    private IncidentService incidentService;

    @Test
    @WithMockUser
    void summary_authenticated_returnsAllCounts() throws Exception {
        when(assetService.countByStatus(AssetStatus.OPERATIONAL)).thenReturn(7L);
        when(assetService.countByStatus(AssetStatus.DEGRADED)).thenReturn(2L);
        when(assetService.countByStatus(AssetStatus.OFFLINE)).thenReturn(1L);
        when(assetService.countByStatus(AssetStatus.MAINTENANCE)).thenReturn(0L);
        when(incidentService.countByStatus(IncidentStatus.OPEN)).thenReturn(3L);
        when(incidentService.countBySeverity(IncidentSeverity.CRITICAL)).thenReturn(1L);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssets").value(10))
                .andExpect(jsonPath("$.assetsOffline").value(1))
                .andExpect(jsonPath("$.openIncidents").value(3))
                .andExpect(jsonPath("$.criticalIncidents").value(1));
    }

    @Test
    void summary_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void summary_zeroCounts_returnsZeros() throws Exception {
        when(assetService.countByStatus(AssetStatus.OPERATIONAL)).thenReturn(0L);
        when(assetService.countByStatus(AssetStatus.DEGRADED)).thenReturn(0L);
        when(assetService.countByStatus(AssetStatus.OFFLINE)).thenReturn(0L);
        when(assetService.countByStatus(AssetStatus.MAINTENANCE)).thenReturn(0L);
        when(incidentService.countByStatus(IncidentStatus.OPEN)).thenReturn(0L);
        when(incidentService.countBySeverity(IncidentSeverity.CRITICAL)).thenReturn(0L);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssets").value(0))
                .andExpect(jsonPath("$.assetsOffline").value(0))
                .andExpect(jsonPath("$.openIncidents").value(0))
                .andExpect(jsonPath("$.criticalIncidents").value(0));
    }
}
