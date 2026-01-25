package com.writingboard.server.domain.meeting.service;

import com.writingboard.server.domain.meeting.dto.request.RoomCreateRequest;
import com.writingboard.server.domain.meeting.dto.request.RoomJoinRequest;
import com.writingboard.server.domain.meeting.dto.response.*;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomInvite;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.InviteType;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import com.writingboard.server.domain.meeting.exception.ErrorCode;
import com.writingboard.server.domain.meeting.exception.MeetingException;
import com.writingboard.server.domain.meeting.repository.RoomInviteRepository;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
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
    private final RoomParticipantRepository participantRepository;
    private final RoomInviteRepository inviteRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회의실 생성
     */
    @Transactional
    public RoomCreateResponse createRoom(Long memberId, RoomCreateRequest request) {
        Member host = getMemberById(memberId);

        String roomUuid = UUID.randomUUID().toString();
        String passwordHash = request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword())
                : null;

        Room room = Room.create(roomUuid, request.getTitle(), host, passwordHash);
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
                throw new MeetingException(ErrorCode.INVALID_PASSWORD);
            }
        }

        Member member = getMemberById(memberId);
        RoomParticipant participant = room.join(member, ParticipantRole.PARTICIPANT);

        return RoomJoinResponse.of(room, participant);
    }

    /**
     * 초대 토큰으로 회의실 입장
     */
    @Transactional
    public RoomJoinResponse joinRoomByInvite(Long memberId, String inviteToken, String password) {
        RoomInvite invite = inviteRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new MeetingException(ErrorCode.INVITE_NOT_FOUND));

        if (!invite.isUsable()) {
            throw new MeetingException(ErrorCode.INVITE_EXPIRED);
        }

        Room room = invite.getRoom();
        validateRoomOpen(room);

        if (room.getPasswordHash() != null && invite.getType() == InviteType.LINK) {
            if (password == null || !passwordEncoder.matches(password, room.getPasswordHash())) {
                throw new MeetingException(ErrorCode.INVALID_PASSWORD);
            }
        }

        Member member = getMemberById(memberId);
        RoomParticipant participant = room.join(member, ParticipantRole.PARTICIPANT);

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
                .orElseThrow(() -> new MeetingException(ErrorCode.NOT_PARTICIPANT));

        if (requester.getRole() != ParticipantRole.HOST &&
                requester.getRole() != ParticipantRole.MODERATOR) {
            throw new MeetingException(ErrorCode.FORBIDDEN);
        }

        if (room.getHost().getId().equals(targetMemberId)) {
            throw new MeetingException(ErrorCode.CANNOT_KICK_HOST);
        }

        Member targetMember = getMemberById(targetMemberId);
        room.leave(targetMember);
    }

    private Room getRoomByUuid(String roomUuid) {
        return roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new MeetingException(ErrorCode.ROOM_NOT_FOUND));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MeetingException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateHost(Room room, Long memberId) {
        if (!room.getHost().getId().equals(memberId)) {
            throw new MeetingException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateRoomOpen(Room room) {
        if (room.getStatus() != RoomStatus.OPEN) {
            throw new MeetingException(ErrorCode.ROOM_CLOSED);
        }
    }
}
