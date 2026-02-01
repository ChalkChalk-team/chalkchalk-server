package com.writingboard.server.domain.drawing.repository;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface DrawingSnapshotRepository extends MongoRepository<DrawingSnapshot, String> {


    Optional<DrawingSnapshot> findFirstByRoomUuidAndPageIndexOrderByVersionDesc(
            String roomUuid,
            Integer pageIndex
    );

    Optional<DrawingSnapshot> findByRoomUuidAndPageIndexAndVersion(
            String roomUuid,
            Integer pageIndex,
            Long version
    );
}
