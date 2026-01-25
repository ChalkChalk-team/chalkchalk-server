package com.writingboard.server.domain.meeting.service;

import com.writingboard.server.domain.meeting.dto.request.InviteLinkRequest;
import com.writingboard.server.domain.meeting.dto.response.InviteLinkResponse;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomInvite;
import com.writingboard.server.domain.meeting.entity.enums.InviteStatus;
import com.writingboard.server.domain.meeting.entity.enums.InviteType;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.exception.ErrorCode;
import com.writingboard.server.domain.meeting.exception.MeetingException;
import com.writingboard.server.domain.meeting.repository.RoomInviteRepository;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomInviteService {

    private static final int DEFAULT_EXPIRE_MINUTES = 5;

    private final RoomRepository roomRepository;
    private final RoomInviteRepository inviteRepository;
    private final RoomParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final InviteTokenRedisService inviteTokenRedisService;

    /**
     * 링크 초대 생성
     */
    @Transactional
    public InviteLinkResponse createLinkInvite(Long memberId, String roomUuid, InviteLinkRequest request) {
        Room room = getRoomByUuid(roomUuid);
        validateParticipant(room, memberId);

        Member issuer = getMemberById(memberId);
        int expireMinutes = request.getExpiresInMinutes() != null ? request.getExpiresInMinutes() : DEFAULT_EXPIRE_MINUTES;

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(expireMinutes, ChronoUnit.MINUTES);

        RoomInvite invite = RoomInvite.issue(
                room, issuer, token, InviteType.LINK, expiresAt, null
        );

        // 1. DB 저장
        inviteRepository.save(invite);
        // 2. Redis 저장
        inviteTokenRedisService.saveInviteToken(token, room.getRoomUuid(), expireMinutes);

        return InviteLinkResponse.of(invite);
    }

    private Room getRoomByUuid(String roomUuid) {
        return roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new MeetingException(ErrorCode.ROOM_NOT_FOUND));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MeetingException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateParticipant(Room room, Long memberId) {
        boolean isParticipant = participantRepository
                .existsByRoomIdAndMemberIdAndState(room.getId(), memberId, ParticipantState.JOINED);
        if (!isParticipant) {
            throw new MeetingException(ErrorCode.NOT_PARTICIPANT);
        }
    }
}
