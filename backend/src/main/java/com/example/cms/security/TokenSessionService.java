package com.example.cms.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenSessionService {

    private static final String SESSION_KEY_PREFIX = "auth:session:";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public void createSession(String sessionId, Long userId) {
        stringRedisTemplate.opsForValue().set(
                sessionKey(sessionId),
                String.valueOf(userId),
                Duration.ofMillis(refreshTokenExpiration)
        );
    }

    public boolean isActive(String sessionId, Long userId) {
        if (!StringUtils.hasText(sessionId) || userId == null) {
            return false;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(sessionKey(sessionId));
            return value != null && value.equals(String.valueOf(userId));
        } catch (Exception e) {
            log.warn("Failed to validate token session: {}", e.getMessage());
            return false;
        }
    }

    public boolean consumeSession(String sessionId, Long userId) {
        if (!StringUtils.hasText(sessionId) || userId == null) {
            return false;
        }
        try {
            String value = stringRedisTemplate.opsForValue().getAndDelete(sessionKey(sessionId));
            return value != null && value.equals(String.valueOf(userId));
        } catch (Exception e) {
            log.warn("Failed to consume token session: {}", e.getMessage());
            return false;
        }
    }

    public void revokeSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        try {
            stringRedisTemplate.delete(sessionKey(sessionId));
        } catch (Exception e) {
            log.warn("Failed to revoke token session: {}", e.getMessage());
        }
    }

    private String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }
}
