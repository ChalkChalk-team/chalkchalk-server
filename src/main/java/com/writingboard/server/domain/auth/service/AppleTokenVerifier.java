package com.writingboard.server.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.writingboard.server.domain.auth.dto.internal.SocialUserInfo;
import com.writingboard.server.domain.auth.exception.AuthErrorCode;
import com.writingboard.server.domain.auth.exception.AuthException;
import com.writingboard.server.domain.member.entity.enums.AuthProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleTokenVerifier implements SocialTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final Duration JWKS_CACHE_TTL = Duration.ofHours(1);

    @Value("${apple.client-id}")
    private String clientId;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private final Object cacheLock = new Object();
    private final Base64.Decoder urlDecoder = Base64.getUrlDecoder();

    private volatile JwksCache jwksCache = JwksCache.EMPTY;

    private record JwksCache(Map<String, PublicKey> keys, Instant expiresAt) {
        static final JwksCache EMPTY = new JwksCache(Map.of(), Instant.EPOCH);

        boolean isValid(String kid) {
            return Instant.now().isBefore(expiresAt) && keys.containsKey(kid);
        }
    }

    @Override
    public SocialUserInfo verify(String idToken) {
        try {
            String kid = extractKid(idToken);
            PublicKey publicKey = getPublicKey(kid);
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            validateClaims(claims);

            String socialId = claims.getSubject();
            String email = claims.get("email", String.class);

            return SocialUserInfo.builder()
                    .socialId(socialId)
                    .email(email)
                    .name(null)
                    .build();

        } catch (AuthException e) {
            throw e;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException |
                 IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
        } catch (Exception e) {
            log.warn("Apple token verification failed: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.APPLE;
    }

    private String extractKid(String idToken) {
        String[] parts = idToken.split("\\.");
        if (parts.length != 3) {
            throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
        }

        try {
            byte[] decodedHeader = urlDecoder.decode(parts[0]);
            JsonNode header = objectMapper.readTree(decodedHeader);
            String kid = header.path("kid").asText(null);
            String alg = header.path("alg").asText(null);

            if (kid == null || kid.isBlank() || !"RS256".equals(alg)) {
                throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
            }

            return kid;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
        }
    }

    private PublicKey getPublicKey(String kid) {
        if (jwksCache.isValid(kid)) {
            return jwksCache.keys().get(kid);
        }

        synchronized (cacheLock) {
            if (jwksCache.isValid(kid)) {
                return jwksCache.keys().get(kid);
            }

            refreshPublicKeys();
            PublicKey publicKey = jwksCache.keys().get(kid);

            if (publicKey == null) {
                refreshPublicKeys();
                publicKey = jwksCache.keys().get(kid);
            }

            if (publicKey == null) {
                throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
            }

            return publicKey;
        }
    }

    private void refreshPublicKeys() {
        try {
            String jwksJson = fetchAppleJwksJson();
            JsonNode root = objectMapper.readTree(jwksJson);
            JsonNode keys = root.path("keys");

            if (!keys.isArray()) {
                throw new IllegalStateException("Apple JWKS keys is not an array");
            }

            Map<String, PublicKey> parsedKeys = new HashMap<>();
            for (JsonNode keyNode : keys) {
                String kid = keyNode.path("kid").asText(null);
                String kty = keyNode.path("kty").asText(null);
                String n = keyNode.path("n").asText(null);
                String e = keyNode.path("e").asText(null);

                if (kid == null || kid.isBlank() || !"RSA".equals(kty) || n == null || e == null) {
                    continue;
                }

                parsedKeys.put(kid, createRsaPublicKey(n, e));
            }

            if (parsedKeys.isEmpty()) {
                throw new IllegalStateException("Apple JWKS is empty");
            }

            jwksCache = new JwksCache(Map.copyOf(parsedKeys), Instant.now().plus(JWKS_CACHE_TTL));
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    protected String fetchAppleJwksJson() {
        return restClient.get()
                .uri(APPLE_JWKS_URL)
                .retrieve()
                .body(String.class);
    }

    private PublicKey createRsaPublicKey(String n, String e) throws Exception {
        BigInteger modulus = new BigInteger(1, urlDecoder.decode(n));
        BigInteger exponent = new BigInteger(1, urlDecoder.decode(e));
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    private void validateClaims(Claims claims) {
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
        }

        if (!isAudienceValid(claims.get("aud"))) {
            throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
        }

        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
        }
    }

    private boolean isAudienceValid(Object audience) {
        if (audience instanceof String aud) {
            return clientId.equals(aud);
        }

        if (audience instanceof Collection<?> audiences) {
            return audiences.stream().anyMatch(clientId::equals);
        }

        return false;
    }
}