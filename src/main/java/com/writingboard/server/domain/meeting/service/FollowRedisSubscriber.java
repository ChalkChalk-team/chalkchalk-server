package com.writingboard.server.domain.meeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.writingboard.server.domain.meeting.dto.FollowStateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowRedisSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "follow:room:";
    private static final String TOPIC_PREFIX = "/topic/room/";
    private static final String TOPIC_SUFFIX = "/follow";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            FollowStateDto followState = objectMapper.readValue(body, FollowStateDto.class);
            String roomUuid = extractRoomUuid(channel);

            String destination = TOPIC_PREFIX + roomUuid + TOPIC_SUFFIX;
            messagingTemplate.convertAndSend(destination, followState);

            log.debug("Follow state sent to clients: destination={}, memberId={}",
                    destination, followState.getMemberId());

        } catch (Exception e) {
            log.error("Failed to process follow message from Redis", e);
        }
    }

    private String extractRoomUuid(String channel) {
        return channel.replace(CHANNEL_PREFIX, "");
    }
}
