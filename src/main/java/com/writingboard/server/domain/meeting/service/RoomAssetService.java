package com.writingboard.server.domain.meeting.service;

import com.writingboard.server.domain.meeting.dto.request.LoadPersonalAssetRequest;
import com.writingboard.server.domain.meeting.dto.request.LoadTeamAssetRequest;
import com.writingboard.server.domain.meeting.dto.request.PageChangeRequest;
import com.writingboard.server.domain.meeting.dto.response.RoomAssetResponse;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomAsset;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.SavePolicy;
import com.writingboard.server.domain.meeting.exception.MeetingErrorCode;
import com.writingboard.server.domain.meeting.exception.MeetingException;
import com.writingboard.server.domain.meeting.repository.RoomAssetRepository;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.personal.entity.PersonalAsset;
import com.writingboard.server.domain.personal.service.PersonalAssetService;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.domain.team.entity.enums.AssetSourceType;
import com.writingboard.server.domain.team.entity.enums.AssetStatus;
import com.writingboard.server.domain.team.repository.TeamAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomAssetService {

    private final RoomAssetRepository roomAssetRepository;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final TeamAssetRepository teamAssetRepository;
    private final MemberRepository memberRepository;
    private final PersonalAssetService personalAssetService;

    @Transactional
    public RoomAssetResponse loadTeamAsset(Long memberId, String roomUuid, LoadTeamAssetRequest request) {
        Room room = getRoomByUuid(roomUuid);
        RoomParticipant participant = validateParticipantWithWritePermission(room, memberId);
        Member member = participant.getMember();

        TeamAsset teamAsset = teamAssetRepository.findByIdAndTeamIdAndStatus(
                        request.getTeamAssetId(), room.getTeam().getId(), AssetStatus.ACTIVE)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ASSET_NOT_FOUND));

        SavePolicy savePolicy = request.getSavePolicy() != null ? request.getSavePolicy() : SavePolicy.OVERWRITE;
        RoomAsset roomAsset = RoomAsset.createFromTeamAsset(room, teamAsset, member, savePolicy);

        roomAssetRepository.save(roomAsset);
        return RoomAssetResponse.from(roomAsset);
    }

    @Transactional
    public RoomAssetResponse loadPersonalAsset(Long memberId, String roomUuid, LoadPersonalAssetRequest request) {
        Room room = getRoomByUuid(roomUuid);
        RoomParticipant participant = validateParticipantWithWritePermission(room, memberId);
        Member member = participant.getMember();
        Team team = room.getTeam();

        PersonalAsset personalAsset = personalAssetService.getAssetEntity(request.getPersonalAssetId());

        if (!personalAsset.isOwnedBy(memberId)) {
            throw new MeetingException(MeetingErrorCode.FORBIDDEN);
        }

        TeamAsset teamAsset = TeamAsset.createImported(
                team,
                member,
                personalAsset.getType(),
                personalAsset.getName(),
                personalAsset.getId(),
                request.getStorageKey(),
                personalAsset.getTotalPages()
        );
        teamAssetRepository.save(teamAsset);

        SavePolicy savePolicy = request.getSavePolicy() != null ? request.getSavePolicy() : SavePolicy.OVERWRITE;
        RoomAsset roomAsset = RoomAsset.createFromTeamAsset(room, teamAsset, member, savePolicy);

        roomAssetRepository.save(roomAsset);
        return RoomAssetResponse.from(roomAsset);
    }

    public List<RoomAssetResponse> getRoomAssets(Long memberId, String roomUuid) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipant(room, memberId);

        return roomAssetRepository.findByRoomUuid(roomUuid).stream()
                .map(RoomAssetResponse::from)
                .toList();
    }

    @Transactional
    public RoomAssetResponse activateAsset(Long memberId, String roomUuid, Long roomAssetId) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipantWithWritePermission(room, memberId);

        RoomAsset roomAsset = roomAssetRepository.findByIdAndRoomUuid(roomAssetId, roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ASSET_NOT_FOUND));

        roomAssetRepository.deactivateAllByRoomUuid(roomUuid);
        roomAsset.activate();

        return RoomAssetResponse.from(roomAsset);
    }

    @Transactional
    public RoomAssetResponse changePage(Long memberId, String roomUuid, Long roomAssetId, PageChangeRequest request) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipantWithWritePermission(room, memberId);

        RoomAsset roomAsset = roomAssetRepository.findByIdAndRoomUuid(roomAssetId, roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ASSET_NOT_FOUND));

        roomAsset.changePage(request.getPageNumber());
        return RoomAssetResponse.from(roomAsset);
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

    private RoomParticipant validateParticipantWithWritePermission(Room room, Long memberId) {
        RoomParticipant participant = participantRepository
                .findByRoomIdAndMemberId(room.getId(), memberId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.NOT_PARTICIPANT));

        if (participant.getState() != ParticipantState.JOINED) {
            throw new MeetingException(MeetingErrorCode.NOT_PARTICIPANT);
        }

        if (participant.getRole() == ParticipantRole.VIEWER) {
            throw new MeetingException(MeetingErrorCode.VIEWER_NOT_ALLOWED);
        }

        return participant;
    }
}
