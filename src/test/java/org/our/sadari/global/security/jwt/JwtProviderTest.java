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
        String token = jwtProvider.createAccessToken(31L, "USER");

        // validateToken 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        assertTrue(jwtProvider.validateToken(token));
        // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(31L, jwtProvider.getUserNumb(token));
        // getRole 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals("USER", jwtProvider.getRole(token));
        // getTokenId 조회로 후속 처리에 필요한 데이터를 가져온다
        assertNotNull(jwtProvider.getTokenId(token));
    }

    /**
     * Refresh Token도 같은 키와 최신 parser API로 검증되고 설정된 유효기간을 초 단위로 반환하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void createAndValidateRefreshToken() {
        // createRefreshToken 호출로 후속 처리에 필요한 객체를 생성한다
        String token = jwtProvider.createRefreshToken(31L);

        // validateToken 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        assertTrue(jwtProvider.validateToken(token));
        // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(31L, jwtProvider.getUserNumb(token));
        // getRefreshTokenValiditySeconds 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(REFRESH_TOKEN_SECONDS, jwtProvider.getRefreshTokenValiditySeconds());
    }

    /**
     * 서명 부분이 조작된 토큰이 validateToken에서 유효한 토큰으로 처리되지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void rejectTamperedToken() {
        // createAccessToken 호출로 후속 처리에 필요한 객체를 생성한다
        String token = jwtProvider.createAccessToken(31L, "USER");
        // Bearer 접두사 뒤에 토큰이 포함되어 있는지 확인한다
        char replacement = token.endsWith("A") ? 'B' : 'A';
        // 요청한 범위의 문자열을 추출한다
        String tamperedToken = token.substring(0, token.length() - 1) + replacement;

        // validateToken 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        assertFalse(jwtProvider.validateToken(tamperedToken));
    }
}
