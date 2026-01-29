package com.writingboard.server.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.writingboard.server.domain.chat.dto.response.ChatMessageDto;
import com.writingboard.server.domain.chat.service.ChatRedisSubscriber;
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
            VoiceRedisSubscriber voiceRedisSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        // chat:room:* 패턴의 모든 채널 구독
        container.addMessageListener(chatRedisSubscriber, new PatternTopic("chat:room:*"));

        // voice:room:* 패턴의 모든 채널 구독
        container.addMessageListener(voiceRedisSubscriber, new PatternTopic("voice:room:*"));

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
}
