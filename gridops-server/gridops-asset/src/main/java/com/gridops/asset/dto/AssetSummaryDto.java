package com.gridops.asset.dto;

import com.gridops.asset.entity.Asset;
import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.entity.AssetType;

public class AssetSummaryDto {

    private Long id;
    private String assetTag;
    private String name;
    private AssetType assetType;
    private AssetStatus status;

    public AssetSummaryDto(Long id, String assetTag, String name,
                           AssetType assetType, AssetStatus status) {
        this.id = id;
        this.assetTag = assetTag;
        this.name = name;
        this.assetType = assetType;
        this.status = status;
    }

    public static AssetSummaryDto fromEntity(Asset asset) {
        return new AssetSummaryDto(
                asset.getId(),
                asset.getAssetTag(),
                asset.getName(),
                asset.getAssetType(),
                asset.getStatus()
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
}
