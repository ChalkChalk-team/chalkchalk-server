package com.writingboard.server.domain.drawing.repository;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

/**
 * 드로잉 스냅샷 MongoDB 레포지토리
 */
public interface DrawingSnapshotRepository extends MongoRepository<DrawingSnapshot, String> {

    /**
     * 특정 페이지의 최신 스냅샷 조회
     */
    @Query("{ 'roomUuid': ?0, 'pageIndex': ?1 }")
    Optional<DrawingSnapshot> findLatestByRoomUuidAndPageIndex(
            String roomUuid,
            Integer pageIndex,
            Pageable pageable
    );

    /**
     * 특정 버전의 스냅샷 조회
     */
    Optional<DrawingSnapshot> findByRoomUuidAndPageIndexAndVersion(
            String roomUuid,
            Integer pageIndex,
            Long version
    );
}
