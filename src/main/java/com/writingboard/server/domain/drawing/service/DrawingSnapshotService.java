package com.writingboard.server.domain.drawing.service;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import com.writingboard.server.domain.drawing.exception.DrawingErrorCode;
import com.writingboard.server.domain.drawing.exception.DrawingException;
import com.writingboard.server.domain.drawing.repository.DrawingSnapshotRepository;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class DrawingSnapshotService {

    private static final String BUFFER_KEY_PREFIX = "drawing:buffer:";

    private final DrawingSnapshotRepository snapshotRepository;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, DrawingStrokeDto> drawingRedisTemplate;
    private final MongoTemplate mongoTemplate;

    /**
     * 스냅샷 저장 및 Redis 버퍼 정리
     */
    @Transactional
    public DrawingSnapshot saveSnapshot(Long memberId, String roomUuid, Long roomAssetId, Integer pageIndex,
                                        Long lastIncludedVersion, String snapshotData) {
        validateSnapshotCreationPermission(memberId, roomUuid);

        Optional<DrawingSnapshot> latestSnapshot = getLatestSnapshot(roomAssetId, pageIndex);

        if (latestSnapshot.isPresent() && latestSnapshot.get().getVersion() >= lastIncludedVersion) {
            log.info("이미 최신 스냅샷이 존재하여 저장을 건너뜁니다. (Current: {}, Incoming: {})",
                    latestSnapshot.get().getVersion(), lastIncludedVersion);
            return latestSnapshot.get();
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.MEMBER_NOT_FOUND));

        DrawingSnapshot saved = upsertLatestSnapshot(
                roomUuid,
                roomAssetId,
                pageIndex,
                lastIncludedVersion,
                snapshotData,
                member
        );

        if (saved.getVersion() < lastIncludedVersion) {
            throw new DrawingException(DrawingErrorCode.SNAPSHOT_SAVE_FAILED);
        }

        log.info("스냅샷 저장 완료: roomUuid={}, roomAssetId={}, pageIndex={}, version={}",
                roomUuid, roomAssetId, pageIndex, saved.getVersion());

        trimRedisBuffer(roomUuid, roomAssetId, pageIndex, saved.getVersion());

        return saved;
    }

    /**
     * 특정 RoomAsset 페이지의 최신 스냅샷 조회
     */
    @Transactional(readOnly = true)
    public Optional<DrawingSnapshot> getLatestSnapshot(Long roomAssetId, Integer pageIndex) {
        return snapshotRepository.findByRoomAssetIdAndPageIndex(roomAssetId, pageIndex);
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

    private DrawingSnapshot upsertLatestSnapshot(String roomUuid, Long roomAssetId, Integer pageIndex,
                                                 Long lastIncludedVersion, String snapshotData, Member member) {
        Query query = Query.query(new Criteria().andOperator(
                Criteria.where("roomAssetId").is(roomAssetId),
                Criteria.where("pageIndex").is(pageIndex),
                new Criteria().orOperator(
                        Criteria.where("version").lt(lastIncludedVersion),
                        Criteria.where("version").exists(false)
                )
        ));

        Update update = new Update()
                .set("roomUuid", roomUuid)
                .set("roomAssetId", roomAssetId)
                .set("pageIndex", pageIndex)
                .set("version", lastIncludedVersion)
                .set("snapshotData", snapshotData)
                .set("createdBy", member.getId())
                .set("createdByName", member.getName())
                .set("createdAt", Instant.now());

        FindAndModifyOptions options = FindAndModifyOptions.options()
                .upsert(true)
                .returnNew(true);

        try {
            DrawingSnapshot updated = mongoTemplate.findAndModify(query, update, options, DrawingSnapshot.class);
            if (updated != null) {
                return updated;
            }
            return getLatestSnapshot(roomAssetId, pageIndex)
                    .orElseThrow(() -> new DrawingException(DrawingErrorCode.SNAPSHOT_SAVE_FAILED));
        } catch (DuplicateKeyException e) {
            log.info("스냅샷 upsert 경쟁 감지: roomAssetId={}, pageIndex={}, incomingVersion={}",
                    roomAssetId, pageIndex, lastIncludedVersion);
            return getLatestSnapshot(roomAssetId, pageIndex)
                    .orElseThrow(() -> new DrawingException(DrawingErrorCode.SNAPSHOT_SAVE_FAILED));
        } catch (DrawingException e) {
            throw e;
        } catch (Exception e) {
            log.error("스냅샷 upsert 실패: roomUuid={}, roomAssetId={}, pageIndex={}, version={}",
                    roomUuid, roomAssetId, pageIndex, lastIncludedVersion, e);
            throw new DrawingException(DrawingErrorCode.SNAPSHOT_SAVE_FAILED);
        }
    }

    private void validateSnapshotCreationPermission(Long memberId, String roomUuid) {
        Room room = roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != RoomStatus.OPEN) {
            throw new DrawingException(DrawingErrorCode.ROOM_CLOSED);
        }

        RoomParticipant participant = participantRepository
                .findByRoomIdAndMemberId(room.getId(), memberId)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.NOT_PARTICIPANT));

        if (participant.getState() != ParticipantState.JOINED) {
            throw new DrawingException(DrawingErrorCode.NOT_PARTICIPANT);
        }

        if (participant.getRole() != ParticipantRole.HOST) {
            throw new DrawingException(DrawingErrorCode.SNAPSHOT_PERMISSION_DENIED);
        }
    }
}
