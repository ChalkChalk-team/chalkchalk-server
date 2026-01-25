package com.writingboard.server.global.config;

import com.writingboard.server.domain.auth.jwt.JwtProvider;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.global.websocket.JwtHandshakeInterceptor;
import com.writingboard.server.global.websocket.StompChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtProvider jwtProvider;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 구독 prefix (SimpleBroker 사용)
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트 발행 prefix -> @MessageMapping 핸들러로 라우팅
        registry.setApplicationDestinationPrefixes("/app");

        // 개인 메시지용 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtProvider))
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
                new StompChannelInterceptor(jwtProvider, roomRepository, participantRepository)
        );
    }
}
