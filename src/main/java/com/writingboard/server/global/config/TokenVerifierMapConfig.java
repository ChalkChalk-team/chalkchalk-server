package com.writingboard.server.global.config;

import com.writingboard.server.domain.auth.service.SocialTokenVerifier;
import com.writingboard.server.domain.member.entity.enums.AuthProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class TokenVerifierMapConfig {

    @Bean
    public Map<AuthProvider, SocialTokenVerifier> socialTokenVerifiers(List<SocialTokenVerifier> socialTokenVerifiers) {
        return socialTokenVerifiers.stream()
                .collect(Collectors.toMap(
                        SocialTokenVerifier::getProvider,
                        socialTokenVerifier -> socialTokenVerifier
                ));
    }
}
