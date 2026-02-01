package com.writingboard.server.domain.meeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteTokenRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "invite:";

    /**
     * 초대 토큰을 Redis에 저장 (TTL : default 5분)
     *
     * @param inviteToken 초대 토큰
     * @param roomUuid    회의실 UUID
     * @param ttlMinutes  만료 시간 (분)
     */
    public void saveInviteToken(String inviteToken, String roomUuid, int ttlMinutes) {
        String key = KEY_PREFIX + inviteToken;
        Duration ttl = Duration.ofMinutes(ttlMinutes);

        redisTemplate.opsForValue().set(key, roomUuid, ttl);
        log.info("초대 토큰 Redis 저장: token={}, roomUuid={}, ttl={}분", inviteToken, roomUuid, ttlMinutes);
    }

    /**
     * 초대 토큰으로 회의실 UUID 조회
     *
     * @param inviteToken 초대 토큰
     * @return 회의실 UUID (없으면 null)
     */
    public String getRoomUuid(String inviteToken) {
        String key = KEY_PREFIX + inviteToken;
        String roomUuid = redisTemplate.opsForValue().get(key);

        if (roomUuid != null) {
            log.debug("초대 토큰 Redis 조회 성공: token={}", inviteToken);
        } else {
            log.debug("초대 토큰 Redis 조회 실패 (없음 또는 만료): token={}", inviteToken);
        }

        return roomUuid;
    }

    /**
     * 초대 토큰 삭제 (회의실 종료 시 등)
     *
     * @param inviteToken 초대 토큰
     */
    public void deleteInviteToken(String inviteToken) {
        String key = KEY_PREFIX + inviteToken;
        Boolean deleted = redisTemplate.delete(key);

        if (deleted) {
            log.info("초대 토큰 Redis 삭제: token={}", inviteToken);
        }
    }
}
