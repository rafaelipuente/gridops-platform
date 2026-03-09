package com.gridops.asset.dto;

import com.gridops.asset.entity.Asset;
import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.entity.AssetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class AssetResponse {

    private Long id;
    private String assetTag;
    private String name;
    private AssetType assetType;
    private AssetStatus status;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate installedDate;
    private Long createdBy;
    private String createdByUsername;
    private Instant createdAt;
    private Instant updatedAt;

    public AssetResponse(Long id, String assetTag, String name, AssetType assetType,
                         AssetStatus status, String location, BigDecimal latitude,
                         BigDecimal longitude, LocalDate installedDate, Long createdBy,
                         String createdByUsername, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.assetTag = assetTag;
        this.name = name;
        this.assetType = assetType;
        this.status = status;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.installedDate = installedDate;
        this.createdBy = createdBy;
        this.createdByUsername = createdByUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AssetResponse fromEntity(Asset asset, String createdByUsername) {
        return new AssetResponse(
                asset.getId(),
                asset.getAssetTag(),
                asset.getName(),
                asset.getAssetType(),
                asset.getStatus(),
                asset.getLocation(),
                asset.getLatitude(),
                asset.getLongitude(),
                asset.getInstalledDate(),
                asset.getCreatedBy(),
                createdByUsername,
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public String getName() {
        return name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public LocalDate getInstalledDate() {
        return installedDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
