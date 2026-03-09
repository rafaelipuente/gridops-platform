package com.gridops.asset.dto;

import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.entity.AssetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetRequest {

    @Size(max = 50)
    private String assetTag;

    @NotBlank
    @Size(max = 100)
    private String name;

    private AssetType assetType;

    private AssetStatus status;

    @Size(max = 255)
    private String location;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDate installedDate;

    public AssetRequest() {
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public LocalDate getInstalledDate() {
        return installedDate;
    }

    public void setInstalledDate(LocalDate installedDate) {
        this.installedDate = installedDate;
    }
}
