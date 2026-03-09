package com.gridops.incident.dto;

import jakarta.validation.constraints.NotNull;

public class IncidentAssignRequest {

    @NotNull
    private Long assignedToUserId;

    public IncidentAssignRequest() {
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }
}
