package com.gridops.asset.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "asset_inspections")
public class AssetInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false, updatable = false)
    private Long assetId;

    @Column(name = "inspected_by", nullable = false, updatable = false)
    private Long inspectedBy;

    @Column(name = "inspection_date", nullable = false, updatable = false)
    private Instant inspectionDate;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private InspectionCondition condition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AssetInspection() {
    }

    public AssetInspection(Long assetId, Long inspectedBy, Instant inspectionDate,
                           String notes, InspectionCondition condition) {
        this.assetId = assetId;
        this.inspectedBy = inspectedBy;
        this.inspectionDate = inspectionDate;
        this.notes = notes;
        this.condition = condition;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetInspection other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AssetInspection{id=" + id + ", assetId=" + assetId + ", condition=" + condition + "}";
    }
}
