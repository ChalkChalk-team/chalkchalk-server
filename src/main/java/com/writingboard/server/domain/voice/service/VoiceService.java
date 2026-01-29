package com.writingboard.server.domain.voice.service;

import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.voice.dto.request.VoiceSignalRequest;
import com.writingboard.server.domain.voice.dto.response.VoiceSignalDto;
import com.writingboard.server.domain.voice.exception.VoiceErrorCode;
import com.writingboard.server.domain.voice.exception.VoiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VoiceService {

    private static final int MAX_SIGNAL_DATA_LENGTH = 10000;

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final VoiceRedisPublisher voiceRedisPublisher;

    /**
     * WebRTC 시그널 전송
     */
    public VoiceSignalDto sendSignal(Long memberId, String roomUuid, VoiceSignalRequest request) {
        validateSignalData(request);

        Room room = roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new VoiceException(VoiceErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != RoomStatus.OPEN) {
            throw new VoiceException(VoiceErrorCode.ROOM_CLOSED);
        }

        validateParticipant(room.getId(), memberId);

        Member sender = memberRepository.findById(memberId)
                .orElseThrow(() -> new VoiceException(VoiceErrorCode.MEMBER_NOT_FOUND));

        VoiceSignalDto signalDto = VoiceSignalDto.of(
                roomUuid,
                sender.getId(),
                sender.getName(),
                request.getSignalType(),
                request.getData()
        );

        voiceRedisPublisher.publish(roomUuid, signalDto);

        log.debug("음성 시그널 전송 완료: roomUuid={}, senderId={}, signalType={}",
                roomUuid, memberId, request.getSignalType());

        return signalDto;
    }

    private void validateSignalData(VoiceSignalRequest request) {
        if (request.getData() == null || request.getData().isBlank()) {
            throw new VoiceException(VoiceErrorCode.INVALID_SIGNAL);
        }
        if (request.getData().length() > MAX_SIGNAL_DATA_LENGTH) {
            throw new VoiceException(VoiceErrorCode.SIGNAL_DATA_TOO_LARGE);
        }
    }

    private void validateParticipant(Long roomId, Long memberId) {
        boolean isParticipant = participantRepository.existsByRoomIdAndMemberIdAndState(
                roomId, memberId, ParticipantState.JOINED);

        if (!isParticipant) {
            throw new VoiceException(VoiceErrorCode.NOT_PARTICIPANT);
        }
    }
}
