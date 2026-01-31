package com.writingboard.server.domain.meeting.repository;

import com.writingboard.server.domain.meeting.entity.CanvasSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CanvasSnapshotRepository extends JpaRepository<CanvasSnapshot, Long> {

    Optional<CanvasSnapshot> findTopByRoomAssetIdAndPageIndexOrderByCreatedAtDesc(Long roomAssetId, Integer pageIndex);

    List<CanvasSnapshot> findByRoomAssetIdAndPageIndexOrderByCreatedAtDesc(Long roomAssetId, Integer pageIndex);

    List<CanvasSnapshot> findByRoomAssetIdOrderByCreatedAtDesc(Long roomAssetId);
}
