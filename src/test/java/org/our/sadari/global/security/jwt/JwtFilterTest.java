package org.our.sadari.global.security.jwt;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : JwtFilterTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 제한 계정 상태의 허용 경로와 인증 Token 용도를 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-09-03        HanWon.Jang        로컬 간편 로그인 필터 제외 검증
 */
@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    // JWT 제공 객체 Mock
    @Mock
    private JwtProvider jwtProvider;
    // Redis Token 서비스 Mock
    @Mock
    private TokenRedisService tokenRedisService;
    // 사용자 데이터 접근 Mock
    @Mock
    private UserMapper userMapper;
    // JWT 필터 단위 테스트 대상
    @InjectMocks
    private JwtFilter jwtFilter;

    /** 정지 회원이 허용된 상태 변경 요청을 위해 CSRF Token을 조회할 수 있음 */
    @Test
    void suspendedCanGetCsrfToken() {
        Boolean allowed = ReflectionTestUtils.invokeMethod(
                jwtFilter, "isSuspendedAllowedPath", "/api/oauth/csrf"
        );

        assertTrue(Boolean.TRUE.equals(allowed));
    }

    /**
     * 로컬 개발용 계정 전환은 브라우저의 기존 Access Token 상태와 무관하게 실행됨
     *
     * @author HanWon.Jang
     */
    @Test
    void localLoginSkipsJwtFilter() {
        // 기존 인증 Cookie를 검사하지 않을 로컬 간편 로그인 요청을 생성함
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/oauth/local-login");
        // 제한 상태 또는 만료된 기존 Cookie가 계정 전환을 막지 않도록 필터 제외 여부를 확인함
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    /** Refresh Token이 accessToken 쿠키에 들어와도 인증 흐름으로 진입하지 않음 */
    @Test
    void refreshRejectedAsAccess() throws Exception {
        // Refresh Token 문자열이 Access Token 쿠키에 잘못 전달된 요청을 생성함
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", "refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = org.mockito.Mockito.mock(FilterChain.class);
        // Access 용도 검증이 Refresh Token을 거부하는 조건을 구성함
        when(jwtProvider.validateAccessToken("refresh-token")).thenReturn(false);

        // JWT 필터의 실제 인증 경계를 실행함
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Access Token 전용 검증과 남은 필터 체인이 호출됐는지 확인함
        verify(jwtProvider).validateAccessToken("refresh-token");
        verify(filterChain).doFilter(request, response);
        // 거부된 토큰으로 Redis 세션이나 사용자 상태를 조회하지 않는지 확인함
        verifyNoInteractions(tokenRedisService, userMapper);
    }
}
