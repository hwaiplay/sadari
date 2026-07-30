package org.our.sadari.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * fileName       : JwtFilterTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 정지 회원의 제한 API와 Kakao 재로그인 접근 범위를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    // JWT 제공 객체 대역
    @Mock
    private JwtProvider jwtProvider;

    // 로그인 토큰과 회원 상태 캐시 서비스 대역
    @Mock
    private TokenRedisService tokenRedisService;

    // 정지 회원 인증 객체 대역
    @Mock
    private Authentication authentication;

    // 후속 서블릿 필터 체인 대역
    @Mock
    private FilterChain filterChain;

    // 정지 회원 API 접근 범위 검증 대상
    @InjectMocks
    private JwtFilter jwtFilter;

    /**
     * 각 테스트 뒤에 현재 스레드의 인증 상태를 제거한다
     *
     * @author SeungHyeon.Kang
     */
    @AfterEach
    void clearSecurityContext() {

        // 다른 테스트가 이전 정지 회원의 인증 상태를 공유하지 않도록 제거한다
        SecurityContextHolder.clearContext();
    }

    /**
     * 정지 회원이 Kakao 로그인 시작 API에 접근할 수 있는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws Exception 서블릿 필터 실행 실패
     */
    @Test
    void suspendedUserCanStartKakaoLogin() throws Exception {

        // 정지 회원의 Kakao 로그인 시작 요청을 준비한다
        MockHttpServletRequest request = createSuspendedRequest("/api/oauth/kakao");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 정지 토큰 쿠키가 있는 Kakao 로그인 요청을 JWT 필터에 전달한다
        jwtFilter.doFilter(request, response, filterChain);

        // Kakao 인증 화면으로 이동할 수 있도록 후속 필터 처리를 계속한다
        verify(filterChain).doFilter(request, response);
        // 허용된 로그인 요청은 정상 응답 상태를 유지한다
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    /**
     * 정지 회원이 Kakao 로그인 콜백을 완료할 수 있는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws Exception 서블릿 필터 실행 실패
     */
    @Test
    void suspendedUserCanCompleteKakaoLoginCallback() throws Exception {

        // 정지 회원의 Kakao 로그인 콜백 요청을 준비한다
        MockHttpServletRequest request = createSuspendedRequest("/api/oauth/callback/kakao");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Kakao에서 돌아온 로그인 콜백을 JWT 필터에 전달한다
        jwtFilter.doFilter(request, response, filterChain);

        // 로그인 결과를 처리할 인증 컨트롤러까지 콜백 요청을 전달한다
        verify(filterChain).doFilter(request, response);
        // 허용된 로그인 콜백은 정상 응답 상태를 유지한다
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    /**
     * 정지 회원의 계정 처리 API 접근은 계속 차단되는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws Exception 서블릿 필터 실행 실패
     */
    @Test
    void suspendedUserCannotAccessWithdrawalApi() throws Exception {

        // 정지 회원의 계정 처리 요청을 준비한다
        MockHttpServletRequest request = createSuspendedRequest("/api/user/withdrawal");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 정지 토큰 쿠키가 있는 계정 처리 요청을 JWT 필터에 전달한다
        jwtFilter.doFilter(request, response, filterChain);

        // 정지 우회가 발생하지 않도록 계정 처리 컨트롤러에는 요청을 전달하지 않는다
        verify(filterChain, never()).doFilter(request, response);
        // 정지 회원의 계정 처리 요청은 권한 없음으로 응답한다
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    /**
     * 정지 회원 Access Token이 포함된 API 요청을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param requestUri 테스트할 API URI
     * @return 정지 회원의 인증 쿠키가 포함된 요청
     */
    private MockHttpServletRequest createSuspendedRequest(String requestUri) {

        // 테스트할 API 경로와 정지 회원 Access Token 쿠키를 설정한다
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(requestUri);
        request.setCookies(new Cookie("accessToken", "suspended-access-token"));

        // 유효한 정지 회원 토큰을 해석하도록 인증 의존성 결과를 설정한다
        when(jwtProvider.validateToken("suspended-access-token")).thenReturn(true);
        when(jwtProvider.getTokenId("suspended-access-token")).thenReturn("suspended-token-id");
        when(tokenRedisService.hasAccessTokenBlacklist("suspended-token-id")).thenReturn(false);
        when(jwtProvider.getAuthentication("suspended-access-token")).thenReturn(authentication);
        when(jwtProvider.getUserNumb("suspended-access-token")).thenReturn(7L);
        when(tokenRedisService.getUserStatus(7L)).thenReturn(Constant.USER_STAT_SUSPENDED);

        // 정지 회원 인증 상태가 준비된 요청을 반환한다
        return request;
    }
}
