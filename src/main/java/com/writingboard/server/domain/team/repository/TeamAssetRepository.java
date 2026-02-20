package com.writingboard.server.domain.team.repository;

import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.domain.team.entity.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TeamAssetRepository extends JpaRepository<TeamAsset, Long> {

    Optional<TeamAsset> findByIdAndStatus(Long id, AssetStatus status);

    Optional<TeamAsset> findByIdAndTeamIdAndStatus(Long id, Long teamId, AssetStatus status);

    boolean existsByIdAndTeamIdAndStatus(Long id, Long teamId, AssetStatus status);

    Page<TeamAsset> findByTeamIdAndStatusAndIsLatestTrue(Long teamId, AssetStatus status, Pageable pageable);

    @Query("SELECT a FROM TeamAsset a WHERE (a.parentAsset.id = :rootAssetId OR a.id = :rootAssetId) AND a.status = :status ORDER BY a.version DESC")
    List<TeamAsset> findVersionHistory(@Param("rootAssetId") Long rootAssetId, @Param("status") AssetStatus status);

    @Query("SELECT a FROM TeamAsset a WHERE a.id = :assetId OR a.parentAsset.id = :assetId ORDER BY a.version DESC")
    List<TeamAsset> findAllVersions(@Param("assetId") Long assetId);

    List<TeamAsset> findByTeamIdAndStatus(Long teamId, AssetStatus status);

    List<TeamAsset> findByStatusAndDeletedAtBeforeAndStorageKeyIsNotNull(AssetStatus status, Instant before);

    void deleteAllByTeamId(Long teamId);
}
