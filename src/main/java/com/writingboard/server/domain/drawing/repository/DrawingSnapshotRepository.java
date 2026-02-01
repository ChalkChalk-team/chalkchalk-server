package com.writingboard.server.domain.drawing.repository;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface DrawingSnapshotRepository extends MongoRepository<DrawingSnapshot, String> {

    /**
     * 특정 RoomAsset 페이지의 최신 스냅샷 조회 (Primary - roomAssetId 기준)
     */
    Optional<DrawingSnapshot> findFirstByRoomAssetIdAndPageIndexOrderByVersionDesc(
            Long roomAssetId,
            Integer pageIndex
    );

    /**
     * 특정 RoomAsset 페이지의 특정 버전 스냅샷 조회
     */
    Optional<DrawingSnapshot> findByRoomAssetIdAndPageIndexAndVersion(
            Long roomAssetId,
            Integer pageIndex,
            Long version
    );

    /**
     * 특정 캔버스 페이지의 최신 스냅샷 조회 (Deprecated - backward compatibility)
     */
    @Deprecated
    Optional<DrawingSnapshot> findFirstByRoomUuidAndPageIndexOrderByVersionDesc(
            String roomUuid,
            Integer pageIndex
    );

    /**
     * 특정 버전의 스냅샷 조회 (Deprecated - backward compatibility)
     */
    @Deprecated
    Optional<DrawingSnapshot> findByRoomUuidAndPageIndexAndVersion(
            String roomUuid,
            Integer pageIndex,
            Long version
    );
}
