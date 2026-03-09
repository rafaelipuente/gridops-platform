package com.gridops.incident.dto;

import com.gridops.incident.entity.IncidentStatus;

import jakarta.validation.constraints.NotNull;

public class IncidentStatusRequest {

    @NotNull
    private IncidentStatus status;

    private String resolutionNotes;

    public IncidentStatusRequest() {
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
