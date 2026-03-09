package com.gridops.asset.dto;

import com.gridops.asset.entity.AssetInspection;
import com.gridops.asset.entity.InspectionCondition;

import java.time.Instant;

public class InspectionResponse {

    private Long id;
    private Long assetId;
    private Long inspectedBy;
    private String inspectedByUsername;
    private Instant inspectionDate;
    private String notes;
    private InspectionCondition condition;
    private Instant createdAt;

    public InspectionResponse(Long id, Long assetId, Long inspectedBy,
                              String inspectedByUsername, Instant inspectionDate,
                              String notes, InspectionCondition condition, Instant createdAt) {
        this.id = id;
        this.assetId = assetId;
        this.inspectedBy = inspectedBy;
        this.inspectedByUsername = inspectedByUsername;
        this.inspectionDate = inspectionDate;
        this.notes = notes;
        this.condition = condition;
        this.createdAt = createdAt;
    }

    public static InspectionResponse fromEntity(AssetInspection inspection,
                                                String inspectedByUsername) {
        return new InspectionResponse(
                inspection.getId(),
                inspection.getAssetId(),
                inspection.getInspectedBy(),
                inspectedByUsername,
                inspection.getInspectionDate(),
                inspection.getNotes(),
                inspection.getCondition(),
                inspection.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public Long getInspectedBy() {
        return inspectedBy;
    }

    public String getInspectedByUsername() {
        return inspectedByUsername;
    }

    public Instant getInspectionDate() {
        return inspectionDate;
    }

    public String getNotes() {
        return notes;
    }

    public InspectionCondition getCondition() {
        return condition;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
