package com.writingboard.server.domain.drawing.service;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import com.writingboard.server.domain.drawing.exception.DrawingErrorCode;
import com.writingboard.server.domain.drawing.exception.DrawingException;
import com.writingboard.server.domain.drawing.repository.DrawingSnapshotRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class DrawingSnapshotService {

    private static final String BUFFER_KEY_PREFIX = "drawing:buffer:";

    private final DrawingSnapshotRepository snapshotRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, DrawingStrokeDto> drawingRedisTemplate;

    /**
     * 스냅샷 저장 및 Redis 버퍼 정리
     */
    @Transactional
    public DrawingSnapshot saveSnapshot(Long memberId, String roomUuid, Long roomAssetId, Integer pageIndex,
                                        Long lastIncludedVersion, String snapshotData) {


        Optional<DrawingSnapshot> latestSnapshot = getLatestSnapshot(roomAssetId, pageIndex);

        if (latestSnapshot.isPresent() && latestSnapshot.get().getVersion() >= lastIncludedVersion) {
            log.info("이미 최신 스냅샷이 존재하여 저장을 건너뜁니다. (Current: {}, Incoming: {})",
                    latestSnapshot.get().getVersion(), lastIncludedVersion);
            return latestSnapshot.get();
        }


        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.MEMBER_NOT_FOUND));

        DrawingSnapshot snapshot = DrawingSnapshot.create(
                roomUuid, roomAssetId, pageIndex, lastIncludedVersion, snapshotData,
                member.getId(), member.getName()
        );

        DrawingSnapshot saved = snapshotRepository.save(snapshot);

        log.info("스냅샷 저장 완료: roomUuid={}, roomAssetId={}, pageIndex={}, version={}",
                roomUuid, roomAssetId, pageIndex, lastIncludedVersion);

        trimRedisBuffer(roomUuid, roomAssetId, pageIndex, lastIncludedVersion);

        return saved;
    }

    /**
     * 특정 RoomAsset 페이지의 최신 스냅샷 조회
     */
    @Transactional(readOnly = true)
    public Optional<DrawingSnapshot> getLatestSnapshot(Long roomAssetId, Integer pageIndex) {
        return snapshotRepository.findFirstByRoomAssetIdAndPageIndexOrderByVersionDesc(roomAssetId, pageIndex);
    }

    /**
     * Redis 버퍼 정리
     */
    private void trimRedisBuffer(String roomUuid, Long roomAssetId, Integer pageIndex, Long lastIncludedVersion) {
        String bufferKey = buildBufferKey(roomUuid, roomAssetId, pageIndex);

        try {
            drawingRedisTemplate.opsForList().trim(bufferKey, lastIncludedVersion, -1);
            log.info("Redis 버퍼 정리 완료: bufferKey={}, trimmedUpTo={}", bufferKey, lastIncludedVersion);
        } catch (Exception e) {
            log.error("Redis 버퍼 정리 실패: bufferKey={}, version={}", bufferKey, lastIncludedVersion, e);
        }
    }

    private String buildBufferKey(String roomUuid, Long roomAssetId, Integer pageIndex) {
        return BUFFER_KEY_PREFIX + "room:" + roomUuid + ":asset:" + roomAssetId + ":page:" + pageIndex;
    }
}