package com.writingboard.server.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.writingboard.server.domain.chat.dto.response.ChatMessageDto;
import com.writingboard.server.domain.chat.service.ChatRedisSubscriber;
import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import com.writingboard.server.domain.drawing.service.DrawingRedisSubscriber;
import com.writingboard.server.domain.meeting.dto.FollowStateDto;
import com.writingboard.server.domain.meeting.service.FollowRedisSubscriber;
import com.writingboard.server.domain.teamchat.dto.response.TeamChatEventDto;
import com.writingboard.server.domain.teamchat.service.TeamChatRedisSubscriber;
import com.writingboard.server.domain.voice.dto.response.VoiceSignalDto;
import com.writingboard.server.domain.voice.service.VoiceRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            ChatRedisSubscriber chatRedisSubscriber,
            DrawingRedisSubscriber drawingRedisSubscriber,
            VoiceRedisSubscriber voiceRedisSubscriber,
            FollowRedisSubscriber followRedisSubscriber,
            TeamChatRedisSubscriber teamChatRedisSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        // chat:room:* 패턴의 모든 채널 구독
        container.addMessageListener(chatRedisSubscriber, new PatternTopic("chat:room:*"));

        // voice:room:* 패턴의 모든 채널 구독
        container.addMessageListener(voiceRedisSubscriber, new PatternTopic("voice:room:*"));

        // drawing:room:* 패턴의 모든 채널 구독
        container.addMessageListener(drawingRedisSubscriber, new PatternTopic("drawing:room:*"));

        // follow:room:* 패턴의 모든 채널 구독
        container.addMessageListener(followRedisSubscriber, new PatternTopic("follow:room:*"));

        // teamchat:team:* 패턴의 모든 채널 구독
        container.addMessageListener(teamChatRedisSubscriber, new PatternTopic("teamchat:team:*"));

        return container;
    }

    @Bean
    public RedisTemplate<String, ChatMessageDto> chatRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, ChatMessageDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<ChatMessageDto> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, ChatMessageDto.class);

        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, VoiceSignalDto> voiceRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, VoiceSignalDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<VoiceSignalDto> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, VoiceSignalDto.class);

        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, DrawingStrokeDto> drawingRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, DrawingStrokeDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<DrawingStrokeDto> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, DrawingStrokeDto.class);

        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, FollowStateDto> followRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, FollowStateDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<FollowStateDto> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, FollowStateDto.class);

        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, TeamChatEventDto> teamChatRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, TeamChatEventDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<TeamChatEventDto> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, TeamChatEventDto.class);

        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }
}
