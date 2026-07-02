package com.seatlock.global.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LXRlc3Qtc2VjcmV0LWtleS0xMjM=";
    private static final String OTHER_SECRET = "b3RoZXItc2VjcmV0LWtleS1vdGhlci1zZWNyZXQta2V5LTQ1Ng==";

    private final JwtTokenProvider provider =
            new JwtTokenProvider(new JwtProperties(SECRET, Duration.ofHours(1)));

    @Test
    @DisplayName("발급한 토큰을 파싱하면 원래 memberId가 복원된다")
    void createAndParse() {
        // given
        String token = provider.createToken(7L);

        // when
        Long memberId = provider.parseMemberId(token);

        // then
        assertThat(memberId).isEqualTo(7L);
    }

    @Test
    @DisplayName("만료된 토큰은 ExpiredJwtException이 발생한다")
    void expiredToken() {
        // given
        JwtTokenProvider expiredProvider =
                new JwtTokenProvider(new JwtProperties(SECRET, Duration.ofSeconds(-1)));
        String token = expiredProvider.createToken(7L);

        // when & then
        assertThatThrownBy(() -> provider.parseMemberId(token))
                .isInstanceOf(ExpiredJwtException.class);

    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 SignatureException이 발생한다")
    void invalidSignature() {
        // given
        JwtTokenProvider otherProvider =
                new JwtTokenProvider(new JwtProperties(OTHER_SECRET, Duration.ofHours(1)));
        String token = otherProvider.createToken(7L);

        // when & then
        assertThatThrownBy(() -> provider.parseMemberId(token))
                .isInstanceOf(SignatureException.class);
    }
}
