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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 드로잉 스냅샷 서비스
 * MongoDB 저장 및 Redis 버퍼 정리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrawingSnapshotService {

    private static final String BUFFER_KEY_PREFIX = "drawing:buffer:";

    private final DrawingSnapshotRepository snapshotRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, DrawingStrokeDto> drawingRedisTemplate;

    /**
     * 스냅샷 저장 및 Redis 버퍼 정리
     */
    @Transactional
    public DrawingSnapshot saveSnapshot(Long memberId, String roomUuid, Integer pageIndex,
                                       Long lastIncludedVersion, String snapshotData) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.MEMBER_NOT_FOUND));

        // 스냅샷 저장
        DrawingSnapshot snapshot = DrawingSnapshot.create(
                roomUuid, pageIndex, lastIncludedVersion, snapshotData,
                member.getId(), member.getName()
        );

        DrawingSnapshot saved = snapshotRepository.save(snapshot);

        log.info("스냅샷 저장 완료: roomUuid={}, pageIndex={}, version={}",
                roomUuid, pageIndex, lastIncludedVersion);

        // Redis 버퍼 정리 (LTRIM)
        trimRedisBuffer(roomUuid, pageIndex, lastIncludedVersion);

        return saved;
    }

    /**
     * 특정 페이지의 최신 스냅샷 조회
     */
    public Optional<DrawingSnapshot> getLatestSnapshot(String roomUuid, Integer pageIndex) {
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "version"));
        return snapshotRepository.findLatestByRoomUuidAndPageIndex(roomUuid, pageIndex, pageRequest);
    }

    /**
     * Redis 버퍼 정리 (LTRIM)
     * lastIncludedVersion까지의 스트로크를 제거
     */
    private void trimRedisBuffer(String roomUuid, Integer pageIndex, Long lastIncludedVersion) {
        String bufferKey = buildBufferKey(roomUuid, pageIndex);

        try {
            // LTRIM: 인덱스 lastIncludedVersion 이후부터 끝까지 유지
            // 예: version 1-50이 저장됨 → LTRIM key 50 -1 (인덱스 50부터 끝까지 유지)
            drawingRedisTemplate.opsForList().trim(bufferKey, lastIncludedVersion, -1);

            log.info("Redis 버퍼 정리 완료: bufferKey={}, trimmedUpTo={}",
                    bufferKey, lastIncludedVersion);

        } catch (Exception e) {
            log.error("Redis 버퍼 정리 실패: bufferKey={}, version={}",
                    bufferKey, lastIncludedVersion, e);
            // 정리 실패해도 스냅샷은 저장되었으므로 예외를 던지지 않음
        }
    }

    /**
     * Redis 버퍼 키 생성
     */
    private String buildBufferKey(String roomUuid, Integer pageIndex) {
        return BUFFER_KEY_PREFIX + roomUuid + ":page:" + pageIndex;
    }
}
