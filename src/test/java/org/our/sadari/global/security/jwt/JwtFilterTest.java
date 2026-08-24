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
 * description    : 제한 계정 상태의 CSRF Token 조회 경로를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
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

    /** 정지 회원이 허용된 상태 변경 요청을 위해 CSRF Token을 조회할 수 있다. */
    @Test
    void suspendedCanGetCsrfToken() {
        Boolean allowed = ReflectionTestUtils.invokeMethod(
                jwtFilter, "isSuspendedAllowedPath", "/api/oauth/csrf"
        );

        assertTrue(Boolean.TRUE.equals(allowed));
    }

    /** Refresh Token이 accessToken 쿠키에 들어와도 인증 흐름으로 진입하지 않는다. */
    @Test
    void refreshRejectedAsAccess() throws Exception {
        // Refresh Token 문자열이 Access Token 쿠키에 잘못 전달된 요청을 생성한다
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", "refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = org.mockito.Mockito.mock(FilterChain.class);
        // Access 용도 검증이 Refresh Token을 거부하는 조건을 구성한다
        when(jwtProvider.validateAccessToken("refresh-token")).thenReturn(false);

        // JWT 필터의 실제 인증 경계를 실행한다
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Access Token 전용 검증과 남은 필터 체인이 호출됐는지 확인한다
        verify(jwtProvider).validateAccessToken("refresh-token");
        verify(filterChain).doFilter(request, response);
        // 거부된 토큰으로 Redis 세션이나 사용자 상태를 조회하지 않는지 확인한다
        verifyNoInteractions(tokenRedisService, userMapper);
    }
}
