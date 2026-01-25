package com.writingboard.server.domain.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempCodeService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration CODE_TTL = Duration.ofSeconds(30);
    private static final String KEY_PREFIX = "auth:code:";

    /**
     * 임시 코드 생성 (30초 TTL)
     *
     * @param accessToken JWT AccessToken
     * @param refreshToken JWT RefreshToken
     * @return 임시 코드 (UUID)
     */
    public String createTempCode(String accessToken, String refreshToken) {
        String code = UUID.randomUUID().toString();
        String key = KEY_PREFIX + code;

        try {
            TokenSet tokenSet = new TokenSet(accessToken, refreshToken);
            String value = objectMapper.writeValueAsString(tokenSet);

            redisTemplate.opsForValue().set(key, value, CODE_TTL);

            log.info("✅ 임시 코드 생성 완료: code={}, ttl={}초", code, CODE_TTL.getSeconds());
            return code;

        } catch (JsonProcessingException e) {
            log.error("❌ Redis 저장 실패 (JSON 변환 오류): code={}", code, e);
            throw new TempCodeException("토큰 저장에 실패했습니다", e);
        } catch (Exception e) {
            log.error("❌ Redis 저장 실패 (알 수 없는 오류): code={}", code, e);
            throw new TempCodeException("임시 코드 생성에 실패했습니다", e);
        }
    }


    public TokenSet exchangeCode(String code) {
        String key = KEY_PREFIX + code;

        try {
            // Atomic: 조회 + 삭제
            String value = redisTemplate.opsForValue().getAndDelete(key);

            if (value == null) {
                log.warn("⚠️ 유효하지 않은 코드 (만료/없음/재사용): code={}", code);
                return null;
            }

            TokenSet tokens = objectMapper.readValue(value, TokenSet.class);
            log.info("✅ 토큰 교환 성공: code={}", code);
            return tokens;

        } catch (JsonProcessingException e) {
            log.error("❌ Redis 조회 실패 (JSON 파싱 오류): code={}", code, e);
            throw new TempCodeException("토큰 조회에 실패했습니다", e);
        } catch (Exception e) {
            log.error("❌ Redis 조회 실패 (알 수 없는 오류): code={}", code, e);
            throw new TempCodeException("토큰 교환에 실패했습니다", e);
        }
    }



    @Getter
    @NoArgsConstructor
    public static class TokenSet {
        private String accessToken;
        private String refreshToken;

        public TokenSet(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}



class TempCodeException extends RuntimeException {
    public TempCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}