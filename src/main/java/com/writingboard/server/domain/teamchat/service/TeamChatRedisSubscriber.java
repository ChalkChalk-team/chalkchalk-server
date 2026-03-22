package com.writingboard.server.domain.teamchat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.writingboard.server.domain.teamchat.dto.response.TeamChatEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamChatRedisSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "teamchat:team:";
    private static final String TOPIC_PREFIX = "/topic/team/";
    private static final String TOPIC_SUFFIX = "/chat";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            TeamChatEventDto event = objectMapper.readValue(body, TeamChatEventDto.class);
            String teamId = channel.replace(CHANNEL_PREFIX, "");

            String destination = TOPIC_PREFIX + teamId + TOPIC_SUFFIX;
            messagingTemplate.convertAndSend(destination, event);

            log.debug("팀 채팅 클라이언트로 메시지 전송: destination={}, eventType={}",
                    destination, event.getEventType());

        } catch (Exception e) {
            log.error("팀 채팅 Redis 메시지 처리 실패", e);
        }
    }
}
