package org.our.sadari.global.security.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.Cookie;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.our.sadari.global.security.jwt.JwtFilter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : SecurityConfigTest
 * author         : OpenAI.Codex
 * date           : 2026-08-04
 * description    : Cookie 인증 API의 CSRF Token 발급과 검증 정책을 확인한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-04        OpenAI.Codex       최초 생성
 */
class SecurityConfigTest {

    // CSRF 보안 설정 단위 테스트 대상
    private SecurityConfig securityConfig;

    /**
     * 운영 환경과 같은 Secure 및 SameSite 속성을 사용하는 보안 설정을 구성한다.
     *
     * @author OpenAI.Codex
     */
    @BeforeEach
    void setUp() {
        // 생성자 의존성만 충족하도록 JWT Filter Mock을 생성한다
        JwtFilter jwtFilter = mock(JwtFilter.class);
        // CSRF Cookie Repository를 검증할 보안 설정을 생성한다
        securityConfig = new SecurityConfig(jwtFilter);
        // 운영 HTTPS 환경과 같은 Secure Cookie 설정을 적용한다
        ReflectionTestUtils.setField(securityConfig, "cookieSecure", true);
        // 분리 출처 운영 환경과 같은 SameSite 정책을 적용한다
        ReflectionTestUtils.setField(securityConfig, "cookieSameSite", "None");
    }

    /**
     * CSRF Token 조회가 HttpOnly와 운영 Cookie 속성을 적용하고 발급한 Token으로 상태 변경 요청을 허용하는지 검증한다.
     *
     * @author OpenAI.Codex
     * @throws Exception Servlet Filter 처리 중 오류가 발생할 때 전달한다
     */
    @Test
    void csrfTokenCookieAllowsRequestWithMatchingHeader() throws Exception {
        // 운영 Cookie 속성을 사용하는 CSRF Token Repository를 조회한다
        CookieCsrfTokenRepository repository = securityConfig.getCsrfTokenRepository();
        // Spring Security의 기본 XOR Token 처리까지 포함할 CSRF Filter를 생성한다
        CsrfFilter csrfFilter = new CsrfFilter(repository);
        // 안전한 GET 요청으로 CSRF Token을 발급받을 요청 객체를 생성한다
        MockHttpServletRequest tokenRequest = new MockHttpServletRequest("GET", "/api/oauth/csrf");
        // 발급된 CSRF Cookie를 확인할 응답 객체를 생성한다
        MockHttpServletResponse tokenResponse = new MockHttpServletResponse();
        // Controller가 조회하는 요청 속성의 CSRF Token을 보관할 참조를 생성한다
        AtomicReference<CsrfToken> tokenReference = new AtomicReference<>();

        // Controller가 요청 속성에서 Token을 조회하는 흐름을 Filter Chain으로 재현한다
        csrfFilter.doFilter(tokenRequest, tokenResponse, (request, response) -> {
            // Spring Security가 요청 속성에 저장한 Token을 조회한다
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            // 지연 발급 Token을 실제 값으로 조회해 Cookie 저장을 완료한다
            csrfToken.getToken();
            // 상태 변경 요청에서 재사용할 Token을 보관한다
            tokenReference.set(csrfToken);
        });

        // 발급 응답에 저장된 CSRF Cookie를 조회한다
        Cookie csrfCookie = tokenResponse.getCookie("XSRF-TOKEN");
        // CSRF Token Cookie가 발급되었는지 확인한다
        assertNotNull(csrfCookie);
        // JavaScript가 CSRF Cookie 원문을 읽을 수 없도록 HttpOnly가 적용되었는지 확인한다
        assertTrue(csrfCookie.isHttpOnly());
        // 운영 HTTPS에서만 CSRF Cookie가 전송되도록 Secure가 적용되었는지 확인한다
        assertTrue(csrfCookie.getSecure());
        // 분리 출처 운영 환경에서 Cookie가 전송되도록 SameSite None이 적용되었는지 확인한다
        assertEquals("None", csrfCookie.getAttribute("SameSite"));

        // 발급 Cookie와 Token Header를 포함할 상태 변경 요청을 생성한다
        MockHttpServletRequest postRequest = new MockHttpServletRequest("POST", "/api/alim/delete-all");
        // 서버가 발급한 CSRF Cookie를 상태 변경 요청에 설정한다
        postRequest.setCookies(csrfCookie);
        // 브라우저가 자동으로 추가하지 않는 Header에 응답으로 받은 Token을 설정한다
        postRequest.addHeader(tokenReference.get().getHeaderName(), tokenReference.get().getToken());
        // 상태 변경 요청의 CSRF 검증 결과를 확인할 응답 객체를 생성한다
        MockHttpServletResponse postResponse = new MockHttpServletResponse();
        // 유효한 Token 요청이 다음 Filter로 전달되었는지 확인할 상태를 생성한다
        AtomicBoolean filterChainCalled = new AtomicBoolean(false);

        // Cookie와 Header가 일치하는 상태 변경 요청을 CSRF Filter에 전달한다
        csrfFilter.doFilter(postRequest, postResponse, (request, response) -> {
            // 유효한 CSRF Token 요청이 다음 Filter까지 도달했음을 기록한다
            filterChainCalled.set(true);
        });

        // 유효한 CSRF Token 요청이 다음 보안 Filter로 전달되었는지 확인한다
        assertTrue(filterChainCalled.get());
    }

    /**
     * CSRF Token Header가 없는 상태 변경 요청을 Spring Security가 거부하는지 검증한다.
     *
     * @author OpenAI.Codex
     * @throws Exception Servlet Filter 처리 중 오류가 발생할 때 전달한다
     */
    @Test
    void csrfFilterRejectsRequestWithoutTokenHeader() throws Exception {
        // 운영 Cookie 속성을 사용하는 CSRF Token Repository를 조회한다
        CookieCsrfTokenRepository repository = securityConfig.getCsrfTokenRepository();
        // 상태 변경 요청의 CSRF 검증을 수행할 Filter를 생성한다
        CsrfFilter csrfFilter = new CsrfFilter(repository);
        // CSRF Token이 없는 상태 변경 요청을 생성한다
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/alim/delete-all");
        // 거부 상태 코드를 확인할 응답 객체를 생성한다
        MockHttpServletResponse response = new MockHttpServletResponse();
        // 거부된 요청이 다음 Filter로 전달되지 않았는지 확인할 상태를 생성한다
        AtomicBoolean filterChainCalled = new AtomicBoolean(false);

        // Token Header가 없는 상태 변경 요청을 CSRF Filter에 전달한다
        csrfFilter.doFilter(request, response, (filterRequest, filterResponse) -> {
            // 취약한 요청이 다음 Filter로 전달되면 실패하도록 호출 상태를 기록한다
            filterChainCalled.set(true);
        });

        // CSRF Token이 없는 요청이 권한 거부 상태로 종료되었는지 확인한다
        assertEquals(403, response.getStatus());
        // 거부된 요청이 다음 보안 Filter로 전달되지 않았는지 확인한다
        assertFalse(filterChainCalled.get());
    }
}
