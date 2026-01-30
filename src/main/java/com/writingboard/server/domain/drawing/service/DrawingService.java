package com.writingboard.server.domain.drawing.service;

import com.writingboard.server.domain.drawing.dto.request.DrawingStrokeRequest;
import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import com.writingboard.server.domain.drawing.enums.DrawingMessageType;
import com.writingboard.server.domain.drawing.exception.DrawingErrorCode;
import com.writingboard.server.domain.drawing.exception.DrawingException;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 드로잉 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrawingService {

    private static final String BUFFER_KEY_PREFIX = "drawing:buffer:";
    private static final String VERSION_KEY_PREFIX = "drawing:version:";
    private static final int MAX_STROKE_DATA_LENGTH = 100000;

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final DrawingRedisPublisher drawingRedisPublisher;
    private final RedisTemplate<String, DrawingStrokeDto> drawingRedisTemplate;

    /**
     * 드로잉 스트로크 전송
     */
    public DrawingStrokeDto sendStroke(Long memberId, String roomUuid, DrawingStrokeRequest request) {
        validateStrokeRequest(request);

        Room room = roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != RoomStatus.OPEN) {
            throw new DrawingException(DrawingErrorCode.ROOM_CLOSED);
        }

        validateParticipant(room.getId(), memberId);

        Member sender = memberRepository.findById(memberId)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.MEMBER_NOT_FOUND));

        // 서버에서 버전 할당 (Redis INCR)
        Long version = assignVersion(roomUuid, request.getPageIndex());

        DrawingStrokeDto strokeDto = DrawingStrokeDto.of(
                roomUuid,
                sender.getId(),
                sender.getName(),
                request.getType(),
                request.getPageIndex(),
                request.getStrokeId(),
                request.getStrokeData(),
                version
        );

        // Redis List에 버퍼링 (페이지별 분리)
        bufferStroke(roomUuid, request.getPageIndex(), strokeDto);

        // Redis Pub/Sub로 실시간 브로드캐스트
        drawingRedisPublisher.publish(roomUuid, strokeDto);

        log.debug("드로잉 스트로크 전송 완료: roomUuid={}, senderId={}, type={}, pageIndex={}",
                roomUuid, memberId, request.getType(), request.getPageIndex());

        return strokeDto;
    }

    /**
     * 특정 페이지의 스트로크 히스토리 조회
     */
    public List<DrawingStrokeDto> getStrokeHistory(String roomUuid, Integer pageIndex) {
        String bufferKey = buildBufferKey(roomUuid, pageIndex);
        List<DrawingStrokeDto> strokes = drawingRedisTemplate.opsForList().range(bufferKey, 0, -1);

        log.debug("스트로크 히스토리 조회: roomUuid={}, pageIndex={}, count={}",
                roomUuid, pageIndex, strokes != null ? strokes.size() : 0);

        return strokes != null ? strokes : List.of();
    }

    /**
     * 특정 페이지의 스트로크 버퍼 초기화
     */
    public void clearStrokes(String roomUuid, Integer pageIndex) {
        String bufferKey = buildBufferKey(roomUuid, pageIndex);
        drawingRedisTemplate.delete(bufferKey);

        log.debug("스트로크 버퍼 초기화: roomUuid={}, pageIndex={}", roomUuid, pageIndex);
    }

    /**
     * 스트로크 요청 데이터 검증
     */
    private void validateStrokeRequest(DrawingStrokeRequest request) {
        DrawingMessageType type = request.getType();

        // ADD, REMOVE 타입은 strokeId 필수
        if ((type == DrawingMessageType.ADD || type == DrawingMessageType.REMOVE)
                && (request.getStrokeId() == null || request.getStrokeId().isBlank())) {
            throw new DrawingException(DrawingErrorCode.STROKE_ID_REQUIRED);
        }

        // ADD, SNAPSHOT 타입은 strokeData 필수
        if ((type == DrawingMessageType.ADD || type == DrawingMessageType.SNAPSHOT)
                && (request.getStrokeData() == null || request.getStrokeData().isBlank())) {
            throw new DrawingException(DrawingErrorCode.STROKE_DATA_REQUIRED);
        }

        // SNAPSHOT 타입은 version 필수
        if (type == DrawingMessageType.SNAPSHOT && request.getVersion() == null) {
            throw new DrawingException(DrawingErrorCode.VERSION_REQUIRED);
        }

        // strokeData 크기 검증
        if (request.getStrokeData() != null
                && request.getStrokeData().length() > MAX_STROKE_DATA_LENGTH) {
            throw new DrawingException(DrawingErrorCode.STROKE_DATA_TOO_LARGE);
        }
    }

    /**
     * 참여자 검증
     */
    private void validateParticipant(Long roomId, Long memberId) {
        boolean isParticipant = participantRepository.existsByRoomIdAndMemberIdAndState(
                roomId, memberId, ParticipantState.JOINED);

        if (!isParticipant) {
            throw new DrawingException(DrawingErrorCode.NOT_PARTICIPANT);
        }
    }

    /**
     * Redis INCR을 사용하여 버전 할당
     */
    private Long assignVersion(String roomUuid, Integer pageIndex) {
        try {
            String versionKey = buildVersionKey(roomUuid, pageIndex);
            Long version = drawingRedisTemplate.opsForValue().increment(versionKey);

            if (version == null) {
                throw new DrawingException(DrawingErrorCode.REDIS_OPERATION_FAILED);
            }

            log.debug("버전 할당: roomUuid={}, pageIndex={}, version={}",
                    roomUuid, pageIndex, version);

            return version;
        } catch (Exception e) {
            log.error("버전 할당 실패: roomUuid={}, pageIndex={}", roomUuid, pageIndex, e);
            throw new DrawingException(DrawingErrorCode.REDIS_OPERATION_FAILED);
        }
    }

    /**
     * 스트로크를 Redis List에 버퍼링 (TTL 없음 - 스냅샷 저장 시까지 유지)
     */
    private void bufferStroke(String roomUuid, Integer pageIndex, DrawingStrokeDto strokeDto) {
        try {
            String bufferKey = buildBufferKey(roomUuid, pageIndex);
            drawingRedisTemplate.opsForList().rightPush(bufferKey, strokeDto);
        } catch (Exception e) {
            log.error("Redis 버퍼링 실패: roomUuid={}, pageIndex={}", roomUuid, pageIndex, e);
            throw new DrawingException(DrawingErrorCode.REDIS_OPERATION_FAILED);
        }
    }

    /**
     * Redis 버전 키 생성
     */
    private String buildVersionKey(String roomUuid, Integer pageIndex) {
        return VERSION_KEY_PREFIX + roomUuid + ":page:" + pageIndex;
    }

    /**
     * Redis 버퍼 키 생성
     */
    private String buildBufferKey(String roomUuid, Integer pageIndex) {
        return BUFFER_KEY_PREFIX + roomUuid + ":page:" + pageIndex;
    }
}
