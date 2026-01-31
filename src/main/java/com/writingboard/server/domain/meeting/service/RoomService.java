package com.writingboard.server.domain.meeting.service;

import com.writingboard.server.domain.meeting.dto.request.RoomCreateRequest;
import com.writingboard.server.domain.meeting.dto.request.RoomJoinRequest;
import com.writingboard.server.domain.meeting.dto.response.*;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomInvite;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import com.writingboard.server.domain.meeting.exception.MeetingErrorCode;
import com.writingboard.server.domain.meeting.exception.MeetingException;
import com.writingboard.server.domain.meeting.repository.RoomInviteRepository;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final RoomParticipantRepository participantRepository;
    private final RoomInviteRepository inviteRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final InviteTokenRedisService inviteTokenRedisService;

    /**
     * 회의실 생성
     */
    @Transactional
    public RoomCreateResponse createRoom(Long memberId, RoomCreateRequest request) {
        Member host = getMemberById(memberId);

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.TEAM_NOT_FOUND));

        // 팀 멤버 검증
        boolean isTeamMember = teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(team.getId(), host.getId(), TeamMemberStatus.ACTIVE);
        if (!isTeamMember) {
            throw new MeetingException(MeetingErrorCode.NOT_TEAM_MEMBER);
        }

        String roomUuid = UUID.randomUUID().toString();
        String passwordHash = request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword())
                : null;

        Room room = Room.create(roomUuid, request.getTitle(), host, team, passwordHash);
        room.join(host, ParticipantRole.HOST);

        roomRepository.save(room);

        return RoomCreateResponse.of(room);
    }

    /**
     * 회의실 종료 (호스트만)
     */
    @Transactional
    public void closeRoom(Long memberId, String roomUuid) {
        Room room = getRoomByUuid(roomUuid);
        validateHost(room, memberId);

        room.getParticipants().forEach(RoomParticipant::leave);
        room.close();
    }

    /**
     * 회의실 상세 조회
     */
    public RoomDetailResponse getRoom(String roomUuid) {
        Room room = getRoomByUuid(roomUuid);
        List<RoomParticipant> activeParticipants =
                participantRepository.findByRoomIdAndState(room.getId(), ParticipantState.JOINED);
        return RoomDetailResponse.of(room, activeParticipants);
    }

    /**
     * 내가 참여중인 회의실 목록
     */
    public List<RoomSummaryResponse> getMyRooms(Long memberId) {
        List<RoomParticipant> myParticipants =
                participantRepository.findMyActiveRooms(memberId, ParticipantState.JOINED);
        return myParticipants.stream()
                .map(p -> RoomSummaryResponse.of(p.getRoom()))
                .toList();
    }

    /**
     * 내가 호스트인 회의실 목록 (히스토리 포함)
     */
    public Page<RoomSummaryResponse> getMyHostedRooms(Long memberId, Pageable pageable) {
        return roomRepository.findByHostId(memberId, pageable)
                .map(RoomSummaryResponse::of);
    }

    /**
     * 회의실 입장
     */
    @Transactional
    public RoomJoinResponse joinRoom(Long memberId, String roomUuid, RoomJoinRequest request) {
        Room room = getRoomByUuid(roomUuid);
        validateRoomOpen(room);

        if (room.getPasswordHash() != null) {
            if (request == null || request.getPassword() == null ||
                    !passwordEncoder.matches(request.getPassword(), room.getPasswordHash())) {
                throw new MeetingException(MeetingErrorCode.INVALID_PASSWORD);
            }
        }

        Member member = getMemberById(memberId);
        RoomParticipant participant = room.join(member, ParticipantRole.PARTICIPANT);

        return RoomJoinResponse.of(room, participant);
    }

    /**
     * 초대 토큰으로 회의실 입장
     * 팀 멤버가 아닌 경우 VIEWER 권한으로 입장
     */
    @Transactional
    public RoomJoinResponse joinRoomByInvite(Long memberId, String inviteToken, String password) {
        Room room;
        RoomInvite invite = null;

        // Redis에서 조회
        String roomUuid = inviteTokenRedisService.getRoomUuid(inviteToken);

        if (roomUuid != null) {
            // Redis에 있음 → 유효한 초대 (Fast Path)
            room = getRoomByUuid(roomUuid);
            // DB에서 초대 정보 조회 (사용 횟수 추적용)
            invite = inviteRepository.findByInviteToken(inviteToken).orElse(null);
        } else {
            // 2차: DB Fallback (Redis 장애 또는 캐시 미스 대비)
            invite = inviteRepository.findByInviteToken(inviteToken)
                    .orElseThrow(() -> new MeetingException(MeetingErrorCode.INVITE_NOT_FOUND));

            // 만료 체크 및 상태 업데이트
            invite.checkAndExpire();

            if (!invite.isUsable()) {
                if (invite.hasUsageLimit() && invite.getUsedCount() >= invite.getMaxUses()) {
                    throw new MeetingException(MeetingErrorCode.INVITE_USAGE_EXCEEDED);
                }
                throw new MeetingException(MeetingErrorCode.INVITE_EXPIRED);
            }

            room = invite.getRoom();
        }

        validateRoomOpen(room);

        // 비밀번호 검증
        if (room.getPasswordHash() != null) {
            if (password == null || !passwordEncoder.matches(password, room.getPasswordHash())) {
                throw new MeetingException(MeetingErrorCode.INVALID_PASSWORD);
            }
        }

        Member member = getMemberById(memberId);

        // 팀 멤버 여부 확인하여 역할 결정
        boolean isTeamMember = teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(
                room.getTeam().getId(), memberId, TeamMemberStatus.ACTIVE);

        ParticipantRole role = isTeamMember ? ParticipantRole.PARTICIPANT : ParticipantRole.VIEWER;

        RoomParticipant participant = room.join(member, role);

        // 초대 사용 횟수 증가
        if (invite != null) {
            invite.use();
        }

        return RoomJoinResponse.of(room, participant);
    }

    /**
     * 회의실 퇴장
     */
    @Transactional
    public void leaveRoom(Long memberId, String roomUuid) {
        Room room = getRoomByUuid(roomUuid);
        Member member = getMemberById(memberId);
        room.leave(member);
    }

    /**
     * 참여자 강제 퇴장 (호스트/모더레이터)
     */
    @Transactional
    public void kickParticipant(Long requesterId, String roomUuid, Long targetMemberId) {
        Room room = getRoomByUuid(roomUuid);

        RoomParticipant requester = participantRepository
                .findByRoomIdAndMemberId(room.getId(), requesterId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.NOT_PARTICIPANT));

        if (requester.getRole() != ParticipantRole.HOST &&
                requester.getRole() != ParticipantRole.MODERATOR) {
            throw new MeetingException(MeetingErrorCode.FORBIDDEN);
        }

        if (room.getHost().getId().equals(targetMemberId)) {
            throw new MeetingException(MeetingErrorCode.CANNOT_KICK_HOST);
        }

        Member targetMember = getMemberById(targetMemberId);
        room.leave(targetMember);
    }

    private Room getRoomByUuid(String roomUuid) {
        return roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.ROOM_NOT_FOUND));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateHost(Room room, Long memberId) {
        if (!room.getHost().getId().equals(memberId)) {
            throw new MeetingException(MeetingErrorCode.FORBIDDEN);
        }
    }

    private void validateRoomOpen(Room room) {
        if (room.getStatus() != RoomStatus.OPEN) {
            throw new MeetingException(MeetingErrorCode.ROOM_CLOSED);
        }
    }
}
