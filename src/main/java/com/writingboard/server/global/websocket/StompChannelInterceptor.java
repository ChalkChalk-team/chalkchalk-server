package com.writingboard.server.global.websocket;

import com.writingboard.server.domain.auth.jwt.JwtProvider;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@Slf4j
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT
                || command == StompCommand.SUBSCRIBE
                || command == StompCommand.SEND
                || command == StompCommand.DISCONNECT) {

            Long memberId = getMemberId(accessor);
            log.debug("[STOMP] cmd={}, sessionId={}, memberId={}, dest={}",
                    command,
                    accessor.getSessionId(),
                    memberId,
                    accessor.getDestination());
        }

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            handleSubscribe(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtProvider.validateToken(token)) {
                Long memberId = jwtProvider.getMemberIdFromToken(token);
                accessor.setUser(new StompPrincipal(memberId));

                log.debug("STOMP CONNECT 성공: memberId={}", memberId);
            } else {
                throw new MessagingException("Invalid JWT token");
            }
        } else {
            Object memberIdAttr = accessor.getSessionAttributes() != null
                    ? accessor.getSessionAttributes().get("memberId")
                    : null;

            if (memberIdAttr instanceof Long memberId) {
                accessor.setUser(new StompPrincipal(memberId)); // @MessageMapping 핸들러에서 Principal로 사용
                log.debug("STOMP CONNECT (세션): memberId={}", memberId);
            }
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/topic/room/")) {
            String roomUuid = extractRoomUuid(destination);
            Long memberId = getMemberId(accessor); // STOMP 헤더에서 멤버 아이디 추출

            if (memberId == null) {
                throw new MessagingException("인증되지 않은 사용자입니다.");
            }

            if (!isParticipant(roomUuid, memberId)) { // 회의실 참여자만 메세지 보내고 받는거 허용
                log.warn("SUBSCRIBE 거부: memberId={}, roomUuid={}, dest={}",
                        memberId, roomUuid, destination);
                throw new MessagingException("해당 회의실의 참여자가 아닙니다.");
            }

            log.debug("SUBSCRIBE 허용: memberId={}, roomUuid={}, dest={}",
                    memberId, roomUuid, destination);
        }
    }

    private String extractRoomUuid(String destination) {
        String[] parts = destination.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }

    private Long getMemberId(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof StompPrincipal principal) {
            return principal.getMemberId();
        }
        return null;
    }

    private boolean isParticipant(String roomUuid, Long memberId) {
        return roomRepository.findByRoomUuid(roomUuid)
                .map(room -> participantRepository.existsByRoomIdAndMemberIdAndState(
                        room.getId(), memberId, ParticipantState.JOINED))
                .orElse(false);
    }
}