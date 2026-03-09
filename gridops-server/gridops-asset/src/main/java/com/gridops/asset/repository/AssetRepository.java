package com.gridops.asset.repository;

import com.gridops.asset.entity.Asset;
import com.gridops.asset.entity.AssetStatus;
import com.gridops.asset.entity.AssetType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByAssetTag(String assetTag);

    Page<Asset> findByAssetType(AssetType assetType, Pageable pageable);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    Page<Asset> findByAssetTypeAndStatus(AssetType assetType, AssetStatus status, Pageable pageable);

    long countByStatus(AssetStatus status);
}
