package com.writingboard.server.domain.chat.service;

import com.writingboard.server.domain.chat.document.ChatMessage;
import com.writingboard.server.domain.chat.dto.response.ChatMessageDto;
import com.writingboard.server.domain.chat.exception.ChatErrorCode;
import com.writingboard.server.domain.chat.exception.ChatException;
import com.writingboard.server.domain.chat.repository.ChatMessageRepository;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final ChatMessageRepository chatMessageRepository;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final ChatRedisPublisher chatRedisPublisher;

    public ChatMessageDto sendMessage(Long memberId, String roomUuid, String content) {
        // 1. 내용 검증
        validateContent(content);

        // 2. XSS 방지
        String sanitizedContent = HtmlUtils.htmlEscape(content);

        // 3. 회의실 조회 및 상태 검증
        Room room = roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != RoomStatus.OPEN) {
            throw new ChatException(ChatErrorCode.ROOM_CLOSED);
        }

        // 4. 참여자 검증
        validateParticipant(room.getId(), memberId);

        // 5. 발신자 조회
        Member sender = memberRepository.findById(memberId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.MEMBER_NOT_FOUND));

        // 6. 메시지 생성 및 저장
        ChatMessage chatMessage = ChatMessage.createText(roomUuid, sender, sanitizedContent);
        chatMessageRepository.save(chatMessage);

        // 7. DTO 변환
        ChatMessageDto messageDto = ChatMessageDto.from(chatMessage);

        // 8. Redis Pub/Sub 발행
        chatRedisPublisher.publish(roomUuid, messageDto);

        log.debug("메시지 전송 완료: roomUuid={}, senderId={}, messageId={}",
                roomUuid, memberId, chatMessage.getId());

        return messageDto;
    }

    public ChatMessageDto sendSystemMessage(String roomUuid, String content) {
        ChatMessage chatMessage = ChatMessage.createSystem(roomUuid, content);
        chatMessageRepository.save(chatMessage);

        ChatMessageDto messageDto = ChatMessageDto.from(chatMessage);
        chatRedisPublisher.publish(roomUuid, messageDto);

        return messageDto;
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ChatException(ChatErrorCode.EMPTY_MESSAGE);
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new ChatException(ChatErrorCode.MESSAGE_TOO_LONG);
        }
    }

    private void validateParticipant(Long roomId, Long memberId) {
        boolean isParticipant = participantRepository.existsByRoomIdAndMemberIdAndState(
                roomId, memberId, ParticipantState.JOINED);

        if (!isParticipant) {
            throw new ChatException(ChatErrorCode.NOT_PARTICIPANT);
        }
    }
}
