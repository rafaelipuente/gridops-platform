package com.gridops.asset.repository;

import com.gridops.asset.entity.AssetInspection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<AssetInspection, Long> {

    Page<AssetInspection> findByAssetIdOrderByInspectionDateDesc(Long assetId, Pageable pageable);
}
