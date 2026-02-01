package com.writingboard.server.domain.meeting.service;

import com.writingboard.server.domain.meeting.dto.request.CanvasUpdateRequest;
import com.writingboard.server.domain.meeting.dto.response.CanvasPageResponse;
import com.writingboard.server.domain.meeting.dto.response.CanvasSnapshotResponse;
import com.writingboard.server.domain.meeting.entity.CanvasPage;
import com.writingboard.server.domain.meeting.entity.CanvasSnapshot;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomAsset;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.SnapshotTriggerType;
import com.writingboard.server.domain.meeting.exception.MeetingErrorCode;
import com.writingboard.server.domain.meeting.exception.MeetingException;
import com.writingboard.server.domain.meeting.repository.CanvasPageRepository;
import com.writingboard.server.domain.meeting.repository.CanvasSnapshotRepository;
import com.writingboard.server.domain.meeting.repository.RoomAssetRepository;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Deprecated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CanvasService {

    private final CanvasPageRepository canvasPageRepository;
    private final CanvasSnapshotRepository canvasSnapshotRepository;
    private final RoomAssetRepository roomAssetRepository;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;

    public CanvasPageResponse getCanvasPage(Long memberId, String roomUuid, Long roomAssetId, int pageIndex) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipant(room, memberId);

        RoomAsset roomAsset = roomAssetRepository.findByIdAndRoomUuid(roomAssetId, roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ASSET_NOT_FOUND));

        CanvasPage canvasPage = canvasPageRepository.findByRoomAssetIdAndPageIndex(roomAsset.getId(), pageIndex)
                .orElse(null);

        if (canvasPage == null) {
            return CanvasPageResponse.empty(roomAssetId, pageIndex);
        }

        return CanvasPageResponse.from(canvasPage);
    }

    @Transactional
    public CanvasPageResponse updateCanvasPage(Long memberId, String roomUuid, Long roomAssetId, int pageIndex, CanvasUpdateRequest request) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipantWithWritePermission(room, memberId);

        RoomAsset roomAsset = roomAssetRepository.findByIdAndRoomUuid(roomAssetId, roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ASSET_NOT_FOUND));

        byte[] rawData = null;
        if (request.getRawData() != null) {
            rawData = Base64.getDecoder().decode(request.getRawData());
        }

        CanvasPage canvasPage = canvasPageRepository.findByRoomAssetIdAndPageIndex(roomAsset.getId(), pageIndex)
                .orElseGet(() -> {
                    CanvasPage newPage = CanvasPage.create(roomAsset, pageIndex);
                    return canvasPageRepository.save(newPage);
                });

        canvasPage.updateData(rawData);

        if (request.isCreateSnapshot()) {
            SnapshotTriggerType triggerType = request.getSnapshotTriggerType() != null
                    ? request.getSnapshotTriggerType()
                    : SnapshotTriggerType.MANUAL;
            createSnapshotInternal(roomAsset, pageIndex, rawData, triggerType);
        }

        return CanvasPageResponse.from(canvasPage);
    }

    @Transactional
    public CanvasSnapshotResponse createSnapshot(Long memberId, String roomUuid, Long roomAssetId, int pageIndex, SnapshotTriggerType triggerType) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipantWithWritePermission(room, memberId);

        RoomAsset roomAsset = roomAssetRepository.findByIdAndRoomUuid(roomAssetId, roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ASSET_NOT_FOUND));

        CanvasPage canvasPage = canvasPageRepository.findByRoomAssetIdAndPageIndex(roomAsset.getId(), pageIndex)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.CANVAS_NOT_FOUND));

        CanvasSnapshot snapshot = createSnapshotInternal(roomAsset, pageIndex, canvasPage.getRawData(), triggerType);
        return CanvasSnapshotResponse.from(snapshot);
    }

    @Transactional
    public CanvasPageResponse restoreFromSnapshot(Long memberId, String roomUuid, Long roomAssetId, int pageIndex) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipantWithWritePermission(room, memberId);

        RoomAsset roomAsset = roomAssetRepository.findByIdAndRoomUuid(roomAssetId, roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ASSET_NOT_FOUND));

        CanvasSnapshot snapshot = canvasSnapshotRepository.findTopByRoomAssetIdAndPageIndexOrderByCreatedAtDesc(roomAsset.getId(), pageIndex)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.SNAPSHOT_NOT_FOUND));

        CanvasPage canvasPage = canvasPageRepository.findByRoomAssetIdAndPageIndex(roomAsset.getId(), pageIndex)
                .orElseGet(() -> {
                    CanvasPage newPage = CanvasPage.create(roomAsset, pageIndex);
                    return canvasPageRepository.save(newPage);
                });

        canvasPage.updateData(snapshot.getStrokeData());
        return CanvasPageResponse.from(canvasPage);
    }

    private CanvasSnapshot createSnapshotInternal(RoomAsset roomAsset, int pageIndex, byte[] data, SnapshotTriggerType triggerType) {
        CanvasSnapshot snapshot = CanvasSnapshot.create(roomAsset, pageIndex, data, triggerType);
        return canvasSnapshotRepository.save(snapshot);
    }

    private Room getRoomByUuid(String roomUuid) {
        return roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ROOM_NOT_FOUND));
    }

    private void validateParticipant(Room room, Long memberId) {
        boolean isParticipant = participantRepository
                .existsByRoomIdAndMemberIdAndState(room.getId(), memberId, ParticipantState.JOINED);
        if (!isParticipant) {
            throw new MeetingException(MeetingErrorCode.NOT_PARTICIPANT);
        }
    }

    private void validateParticipantWithWritePermission(Room room, Long memberId) {
        RoomParticipant participant = participantRepository
                .findByRoomIdAndMemberId(room.getId(), memberId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.NOT_PARTICIPANT));

        if (participant.getState() != ParticipantState.JOINED) {
            throw new MeetingException(MeetingErrorCode.NOT_PARTICIPANT);
        }

        if (participant.getRole() == ParticipantRole.VIEWER) {
            throw new MeetingException(MeetingErrorCode.VIEWER_NOT_ALLOWED);
        }
    }
}
