package com.gridops.app.dto;

public class DashboardResponse {

    private long totalAssets;
    private long assetsOffline;
    private long openIncidents;
    private long criticalIncidents;

    public DashboardResponse(long totalAssets, long assetsOffline,
                             long openIncidents, long criticalIncidents) {
        this.totalAssets = totalAssets;
        this.assetsOffline = assetsOffline;
        this.openIncidents = openIncidents;
        this.criticalIncidents = criticalIncidents;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public long getAssetsOffline() {
        return assetsOffline;
    }

    public long getOpenIncidents() {
        return openIncidents;
    }

    public long getCriticalIncidents() {
        return criticalIncidents;
    }
}
