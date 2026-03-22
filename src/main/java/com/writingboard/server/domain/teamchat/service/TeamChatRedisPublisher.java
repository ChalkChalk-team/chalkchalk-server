package com.writingboard.server.domain.teamchat.service;

import com.writingboard.server.domain.teamchat.dto.response.TeamChatEventDto;
import com.writingboard.server.domain.teamchat.dto.response.TeamChatMessageResponse;
import com.writingboard.server.domain.teamchat.enums.TeamChatEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamChatRedisPublisher {

    private static final String CHANNEL_PREFIX = "teamchat:team:";

    private final RedisTemplate<String, TeamChatEventDto> teamChatRedisTemplate;

    public void publishNewMessage(Long teamId, TeamChatMessageResponse message) {
        TeamChatEventDto event = TeamChatEventDto.builder()
                .eventType(TeamChatEventType.NEW_MESSAGE)
                .message(message)
                .build();

        publish(teamId, event);
    }

    public void publishDeleteMessage(Long teamId, String messageId) {
        TeamChatEventDto event = TeamChatEventDto.builder()
                .eventType(TeamChatEventType.DELETE_MESSAGE)
                .messageId(messageId)
                .build();

        publish(teamId, event);
    }

    private void publish(Long teamId, TeamChatEventDto event) {
        String channel = CHANNEL_PREFIX + teamId;

        try {
            teamChatRedisTemplate.convertAndSend(channel, event);
            log.debug("팀 채팅 Redis 메시지 발행: channel={}, eventType={}", channel, event.getEventType());
        } catch (Exception e) {
            log.error("팀 채팅 Redis 메시지 발행 실패: channel={}", channel, e);
        }
    }
}
