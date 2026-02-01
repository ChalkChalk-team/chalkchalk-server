package com.writingboard.server.domain.meeting.repository;

import com.writingboard.server.domain.meeting.entity.CanvasPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CanvasPageRepository extends JpaRepository<CanvasPage, Long> {

    Optional<CanvasPage> findByRoomAssetIdAndPageIndex(Long roomAssetId, Integer pageIndex);

    List<CanvasPage> findByRoomAssetId(Long roomAssetId);

    boolean existsByRoomAssetIdAndPageIndex(Long roomAssetId, Integer pageIndex);
}
