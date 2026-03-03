package com.writingboard.server.domain.meeting.service;

import com.writingboard.server.domain.meeting.dto.FollowStateDto;
import com.writingboard.server.domain.meeting.dto.response.ParticipantViewingResponse;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomAsset;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.exception.MeetingErrorCode;
import com.writingboard.server.domain.meeting.exception.MeetingException;
import com.writingboard.server.domain.meeting.repository.RoomAssetRepository;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Deprecated
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CanvasFollowService {

    private static final String VIEWING_STATE_KEY_PREFIX = "viewing:room:";
    private static final String FOLLOW_KEY_PREFIX = "follow:member:";
    private static final Duration VIEWING_STATE_TTL = Duration.ofMinutes(30);

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final RoomAssetRepository roomAssetRepository;
    private final CanvasFollowRedisPublisher canvasFollowRedisPublisher;
    private final RedisTemplate<String, String> stringRedisTemplate;

    public void updateViewingState(Long memberId, String roomUuid, Long roomAssetId, Integer pageIndex) {
        Room room = getRoomByUuid(roomUuid);
        RoomParticipant participant = validateParticipant(room, memberId);

        String key = VIEWING_STATE_KEY_PREFIX + roomUuid + ":" + memberId;
        String value = roomAssetId + ":" + pageIndex;
        stringRedisTemplate.opsForValue().set(key, value, VIEWING_STATE_TTL);

        FollowStateDto state = FollowStateDto.of(
                memberId,
                participant.getMember().getDisplayName(),
                roomAssetId,
                pageIndex
        );
        canvasFollowRedisPublisher.publish(roomUuid, state);

        log.debug("Updated viewing state: memberId={}, roomUuid={}, assetId={}, page={}",
                memberId, roomUuid, roomAssetId, pageIndex);
    }

    @Transactional
    public void startFollow(Long memberId, String roomUuid, Long targetMemberId) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipant(room, memberId);

        RoomParticipant target = participantRepository.findByRoomIdAndMemberId(room.getId(), targetMemberId)
                .filter(p -> p.getState() == ParticipantState.JOINED)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEMBER_NOT_FOUND));

        String followKey = FOLLOW_KEY_PREFIX + memberId + ":room:" + roomUuid;
        stringRedisTemplate.opsForValue().set(followKey, targetMemberId.toString(), VIEWING_STATE_TTL);

        log.info("Member {} started following {} in room {}", memberId, targetMemberId, roomUuid);
    }

    @Transactional
    public void stopFollow(Long memberId, String roomUuid) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipant(room, memberId);

        String followKey = FOLLOW_KEY_PREFIX + memberId + ":room:" + roomUuid;
        stringRedisTemplate.delete(followKey);

        log.info("Member {} stopped following in room {}", memberId, roomUuid);
    }

    public Long getFollowingTarget(Long memberId, String roomUuid) {
        String followKey = FOLLOW_KEY_PREFIX + memberId + ":room:" + roomUuid;
        String targetIdStr = stringRedisTemplate.opsForValue().get(followKey);

        if (targetIdStr == null) {
            return null;
        }

        return Long.parseLong(targetIdStr);
    }

    public List<ParticipantViewingResponse> getParticipantsViewing(Long memberId, String roomUuid) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipant(room, memberId);

        List<RoomParticipant> activeParticipants = participantRepository
                .findByRoomIdAndState(room.getId(), ParticipantState.JOINED);

        Map<Long, RoomAsset> activeAssets = roomAssetRepository.findByRoomUuid(roomUuid).stream()
                .filter(RoomAsset::getIsActive)
                .collect(Collectors.toMap(RoomAsset::getId, a -> a, (a, b) -> a));

        return activeParticipants.stream()
                .map(participant -> {
                    Long participantMemberId = participant.getMember().getId();
                    String key = VIEWING_STATE_KEY_PREFIX + roomUuid + ":" + participantMemberId;
                    String value = stringRedisTemplate.opsForValue().get(key);

                    Long roomAssetId = null;
                    Integer pageIndex = null;
                    String assetName = null;

                    if (value != null) {
                        String[] parts = value.split(":");
                        if (parts.length == 2) {
                            roomAssetId = Long.parseLong(parts[0]);
                            pageIndex = Integer.parseInt(parts[1]);

                            RoomAsset asset = activeAssets.get(roomAssetId);
                            if (asset != null && asset.getTeamAsset() != null) {
                                assetName = asset.getTeamAsset().getName();
                            }
                        }
                    }

                    return ParticipantViewingResponse.builder()
                            .memberId(participantMemberId)
                            .memberName(participant.getMember().getDisplayName())
                            .role(participant.getRole())
                            .roomAssetId(roomAssetId)
                            .assetName(assetName)
                            .pageIndex(pageIndex)
                            .build();
                })
                .toList();
    }

    private Room getRoomByUuid(String roomUuid) {
        return roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ROOM_NOT_FOUND));
    }

    private RoomParticipant validateParticipant(Room room, Long memberId) {
        RoomParticipant participant = participantRepository
                .findByRoomIdAndMemberId(room.getId(), memberId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.NOT_PARTICIPANT));

        if (participant.getState() != ParticipantState.JOINED) {
            throw new MeetingException(MeetingErrorCode.NOT_PARTICIPANT);
        }

        return participant;
    }
}
