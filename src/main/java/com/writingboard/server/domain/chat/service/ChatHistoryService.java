package com.writingboard.server.domain.chat.service;

import com.writingboard.server.domain.chat.document.ChatMessage;
import com.writingboard.server.domain.chat.dto.response.ChatHistoryResponse;
import com.writingboard.server.domain.chat.dto.response.ChatMessageDto;
import com.writingboard.server.domain.chat.exception.ChatErrorCode;
import com.writingboard.server.domain.chat.exception.ChatException;
import com.writingboard.server.domain.chat.repository.ChatMessageRepository;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final ChatMessageRepository chatMessageRepository;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;

    /**
     * 채팅 기록 조회 (커서 기반 페이징)
     * - 진행 중인 회의실: 현재 참여자만 조회 가능
     * - 종료된 회의실: 참여 이력이 있는 사용자만 조회 가능
     */
    public ChatHistoryResponse getMessages(Long memberId, String roomUuid,
                                            Instant before, Integer limit) {
        // 1. 회의실 조회
        Room room = roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));

        // 2. 참여 이력 검증 (현재 참여 또는 과거 참여)
        boolean hasParticipationHistory = participantRepository.findByRoomIdAndMemberId(
                room.getId(), memberId).isPresent();

        if (!hasParticipationHistory) {
            throw new ChatException(ChatErrorCode.NOT_PARTICIPANT_HISTORY);
        }

        // 3. 페이징 설정
        int actualLimit = resolveLimit(limit);
        Pageable pageable = PageRequest.of(0, actualLimit, Sort.by(Sort.Direction.DESC, "timestamp"));

        // 4. 메시지 조회
        List<ChatMessage> messages;
        if (before != null) {
            // 커서 기반: before 시점 이전 메시지
            messages = chatMessageRepository.findByRoomUuidAndTimestampBefore(
                    roomUuid, before, pageable);
        } else {
            // 초기 로드: 최신 메시지
            messages = chatMessageRepository.findByRoomUuidOrderByTimestampDesc(
                    roomUuid, pageable);
        }

        // 5. DTO 변환 및 응답
        List<ChatMessageDto> messageDtos = messages.stream()
                .map(ChatMessageDto::from)
                .toList();

        log.debug("채팅 기록 조회: roomUuid={}, memberId={}, count={}",
                roomUuid, memberId, messageDtos.size());

        return ChatHistoryResponse.of(messageDtos, actualLimit);
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
