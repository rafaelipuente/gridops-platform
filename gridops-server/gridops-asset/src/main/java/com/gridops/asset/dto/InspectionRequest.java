package com.gridops.asset.dto;

import com.gridops.asset.entity.InspectionCondition;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class InspectionRequest {

    @NotNull
    private Instant inspectionDate;

    private String notes;

    @NotNull
    private InspectionCondition condition;

    public InspectionRequest() {
    }

    public Instant getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Instant inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public InspectionCondition getCondition() {
        return condition;
    }

    public void setCondition(InspectionCondition condition) {
        this.condition = condition;
    }
}
