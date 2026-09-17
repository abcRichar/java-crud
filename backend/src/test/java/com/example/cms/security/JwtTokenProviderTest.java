package com.example.cms.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    @Test
    void includesSessionIdInAccessAndRefreshTokens() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(provider, "accessTokenExpiration", 60_000L);
        ReflectionTestUtils.setField(provider, "refreshTokenExpiration", 120_000L);
        provider.init();

        String accessToken = provider.generateAccessToken(7L, "tester", "session-1");
        String refreshToken = provider.generateRefreshToken(7L, "tester", "session-1");

        assertThat(provider.getUserIdFromToken(accessToken)).isEqualTo(7L);
        assertThat(provider.getTokenTypeFromToken(accessToken)).isEqualTo("ACCESS");
        assertThat(provider.getSessionIdFromToken(accessToken)).isEqualTo("session-1");
        assertThat(provider.getTokenTypeFromToken(refreshToken)).isEqualTo("REFRESH");
        assertThat(provider.getSessionIdFromToken(refreshToken)).isEqualTo("session-1");
    }
}
