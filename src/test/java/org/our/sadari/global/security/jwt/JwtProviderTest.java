package org.our.sadari.global.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JJWT API와 런타임 구현체가 같은 계약으로 토큰을 생성하고 검증하는지 확인합니다.
 *
 * @author Seunghyeon.Kang
 */
class JwtProviderTest {

    private static final long ACCESS_TOKEN_SECONDS = 1_800L;
    private static final long REFRESH_TOKEN_SECONDS = 86_400L;
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "sadari-jwt-test-secret-key-32-byte".getBytes(StandardCharsets.UTF_8)
    );

    private JwtProvider jwtProvider;

    /**
     * 각 테스트에서 256비트 이상의 동일한 HS256 키로 JwtProvider를 초기화합니다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(TEST_SECRET, ACCESS_TOKEN_SECONDS, REFRESH_TOKEN_SECONDS);
        jwtProvider.initKey();
    }

    /**
     * Access Token의 서명 검증과 사용자·권한·토큰 식별자 추출이 모두 동작하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void createAndParseAccessToken() {
        String token = jwtProvider.createAccessToken(31L, "USER");

        assertTrue(jwtProvider.validateToken(token));
        assertEquals(31L, jwtProvider.getUserNumb(token));
        assertEquals("USER", jwtProvider.getRole(token));
        assertNotNull(jwtProvider.getTokenId(token));
    }

    /**
     * Refresh Token도 같은 키와 최신 parser API로 검증되고 설정된 유효기간을 초 단위로 반환하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void createAndValidateRefreshToken() {
        String token = jwtProvider.createRefreshToken(31L);

        assertTrue(jwtProvider.validateToken(token));
        assertEquals(31L, jwtProvider.getUserNumb(token));
        assertEquals(REFRESH_TOKEN_SECONDS, jwtProvider.getRefreshTokenValiditySeconds());
    }

    /**
     * 서명 부분이 조작된 토큰이 validateToken에서 유효한 토큰으로 처리되지 않는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void rejectTamperedToken() {
        String token = jwtProvider.createAccessToken(31L, "USER");
        char replacement = token.endsWith("A") ? 'B' : 'A';
        String tamperedToken = token.substring(0, token.length() - 1) + replacement;

        assertFalse(jwtProvider.validateToken(tamperedToken));
    }
}
