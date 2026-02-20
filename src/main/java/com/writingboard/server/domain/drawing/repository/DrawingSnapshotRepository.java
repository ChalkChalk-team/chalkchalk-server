package com.writingboard.server.domain.drawing.repository;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface DrawingSnapshotRepository extends MongoRepository<DrawingSnapshot, String> {

    Optional<DrawingSnapshot> findByRoomAssetIdAndPageIndex(
            Long roomAssetId,
            Integer pageIndex
    );

    Optional<DrawingSnapshot> findFirstByRoomAssetIdAndPageIndexOrderByVersionDesc(
            Long roomAssetId,
            Integer pageIndex
    );

    Optional<DrawingSnapshot> findByRoomAssetIdAndPageIndexAndVersion(
            Long roomAssetId,
            Integer pageIndex,
            Long version
    );


    @Deprecated
    Optional<DrawingSnapshot> findFirstByRoomUuidAndPageIndexOrderByVersionDesc(
            String roomUuid,
            Integer pageIndex
    );

    @Deprecated
    Optional<DrawingSnapshot> findByRoomUuidAndPageIndexAndVersion(
            String roomUuid,
            Integer pageIndex,
            Long version
    );
}
