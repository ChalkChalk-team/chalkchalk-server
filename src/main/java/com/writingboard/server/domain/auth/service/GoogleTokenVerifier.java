package com.writingboard.server.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.writingboard.server.domain.auth.dto.internal.SocialUserInfo;
import com.writingboard.server.domain.auth.exception.AuthErrorCode;
import com.writingboard.server.domain.auth.exception.AuthException;
import com.writingboard.server.domain.member.entity.enums.AuthProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GoogleTokenVerifier implements SocialTokenVerifier {

    @Value("${google.client-id}")
    private String clientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() {
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(List.of(clientId))
                .build();
    }

    @Override
    public SocialUserInfo verify(String idToken) {
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            log.info("Google token verified - socialId: {}", payload.getSubject());

            return SocialUserInfo.builder()
                    .socialId(payload.getSubject())
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .build();

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google token verification failed: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }
}
