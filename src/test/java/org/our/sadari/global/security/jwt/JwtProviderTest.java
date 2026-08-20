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
 * fileName       : JwtProviderTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 인증과 보안 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 * 2026-08-16        SeungHyeon.Kang    JWT 서명 조작 검증의 Base64URL 비결정성 제거
 */
class JwtProviderTest {

    // 접근 TOKEN SECONDS 설정값
    private static final long ACCESS_TOKEN_SECONDS = 1_800L;
    // REFRESH TOKEN SECONDS 설정값
    private static final long REFRESH_TOKEN_SECONDS = 86_400L;
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            // 암호화 또는 전송에 사용할 바이트 배열로 변환한다
            "sadari-jwt-test-secret-key-32-byte".getBytes(StandardCharsets.UTF_8)
    );

    // Jwt 외부 연동 제공 객체
    private JwtProvider jwtProvider;

    /**
     * 각 테스트에서 256비트 이상의 동일한 HS256 키로 JwtProvider를 초기화한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // JWT 발급과 검증 테스트 대상을 담을 객체를 생성한다
        jwtProvider = new JwtProvider(TEST_SECRET, ACCESS_TOKEN_SECONDS, REFRESH_TOKEN_SECONDS);
        // 토큰 생성과 검증에 사용할 서명 키를 초기화한다
        jwtProvider.initKey();
    }

    /**
     * Access Token의 서명 검증과 사용자·권한·토큰 식별자 추출이 모두 동작하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void createAndParseAccessToken() {
        // createAccessToken 호출로 후속 처리에 필요한 객체를 생성한다
        String token = jwtProvider.createAccessToken(31L, "USER", "session-1");

        // validateToken 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        assertTrue(jwtProvider.validateToken(token));
        // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(31L, jwtProvider.getUserNumb(token));
        // getRole 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals("USER", jwtProvider.getRole(token));
        // getTokenId 조회로 후속 처리에 필요한 데이터를 가져온다
        assertNotNull(jwtProvider.getTokenId(token));
        // 기기별 세션 식별자가 Access Token에 포함되는지 검증한다
        assertEquals("session-1", jwtProvider.getSessionId(token));
    }

    /**
     * Refresh Token도 같은 키와 최신 parser API로 검증되고 설정된 유효기간을 초 단위로 반환하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void validateNewRefreshToken() {
        // createRefreshToken 호출로 후속 처리에 필요한 객체를 생성한다
        String token = jwtProvider.createRefreshToken(31L, "session-1");

        // validateToken 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        assertTrue(jwtProvider.validateToken(token));
        // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(31L, jwtProvider.getUserNumb(token));
        // Access Token과 같은 기기 세션 식별자가 Refresh Token에도 포함되는지 검증한다
        assertEquals("session-1", jwtProvider.getSessionId(token));
        // getRefreshTokenValidSec 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(REFRESH_TOKEN_SECONDS, jwtProvider.getRefreshTokenValidSec());
    }

    /**
     * 서명 부분이 조작된 토큰이 validateToken에서 유효한 토큰으로 처리되지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void rejectTamperedToken() {
        // createAccessToken 호출로 후속 처리에 필요한 객체를 생성한다
        String token = jwtProvider.createAccessToken(31L, "USER", "session-1");
        // Base64URL 끝의 미사용 비트를 피하도록 서명의 첫 문자를 찾는다
        int signatureStart = token.lastIndexOf('.') + 1;
        // 실제 서명 바이트가 달라지도록 첫 문자를 다른 값으로 교체한다
        char replacement = token.charAt(signatureStart) == 'A' ? 'B' : 'A';
        // 헤더와 페이로드는 유지하면서 서명의 첫 문자만 조작한다
        String tamperedToken = token.substring(0, signatureStart)
                + replacement
                + token.substring(signatureStart + 1);

        // validateToken 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        assertFalse(jwtProvider.validateToken(tamperedToken));
    }
}
