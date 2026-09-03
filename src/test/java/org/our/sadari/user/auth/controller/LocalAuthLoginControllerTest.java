package org.our.sadari.user.auth.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.user.auth.service.LocalAuthService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : LocalAuthLoginControllerTest
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : loc 프로필 제한과 localhost 및 Tailnet 로그인 이동을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 */
@ExtendWith(MockitoExtension.class)
class LocalAuthLoginControllerTest {

    // 로컬 개발용 로그인 서비스 대역
    @Mock
    private LocalAuthService localAuthService;
    // 로컬 개발용 로그인 컨트롤러 단위 테스트 대상
    private LocalAuthLoginController localAuthLoginController;

    /**
     * localhost와 Tailnet 리다이렉트 및 인증 Cookie 설정을 구성한다
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUp() {
        // 로컬 로그인 서비스 대역을 사용하는 컨트롤러를 생성한다
        localAuthLoginController = new LocalAuthLoginController(localAuthService);
        // Tailnet 로그인 완료 화면 주소를 설정한다
        ReflectionTestUtils.setField(localAuthLoginController, "frontDomain", "https://device.tailnet.example");
        // localhost 로그인 완료 화면 주소를 설정한다
        ReflectionTestUtils.setField(localAuthLoginController, "localFrontDomain", "http://localhost:5173");
        // Access Token Cookie 유지 시간을 설정한다
        ReflectionTestUtils.setField(localAuthLoginController, "accessTokenCookieMaxAgeSeconds", 1800L);
        // Refresh Token Cookie 유지 시간을 설정한다
        ReflectionTestUtils.setField(localAuthLoginController, "refreshTokenCookieMaxAgeSeconds", 3600L);
        // HTTP 테스트에서도 Cookie를 확인할 수 있도록 Secure 속성을 해제한다
        ReflectionTestUtils.setField(localAuthLoginController, "cookieSecure", false);
        // 기존 OAuth 인증 Cookie와 같은 SameSite 속성을 설정한다
        ReflectionTestUtils.setField(localAuthLoginController, "cookieSameSite", "Lax");
    }

    /**
     * 로컬 개발용 로그인 컨트롤러는 loc 프로필에서만 Bean으로 등록된다
     *
     * @author HanWon.Jang
     */
    @Test
    void controllerUsesLocProfile() {
        // 컨트롤러 클래스에 선언된 Spring Profile 조건을 조회한다
        Profile profile = LocalAuthLoginController.class.getAnnotation(Profile.class);
        // 운영 프로필에서 생성되지 않도록 loc 조건만 선언되었는지 확인한다
        assertArrayEquals(new String[]{"loc & !prod"}, profile.value());
    }

    /**
     * Vite가 전달한 localhost Host는 로컬 화면으로 복귀하고 인증 Cookie를 저장한다
     *
     * @author HanWon.Jang
     * @throws Exception 로그인 완료 리다이렉트 응답 기록에 실패할 때 발생
     */
    @Test
    void localhostRedirectsLocal() throws Exception {
        // 활성 회원의 개발용 로그인 성공 결과를 구성한다
        when(localAuthService.setLocalLogin(101L)).thenReturn(getLoginResult());
        // changeOrigin 이전의 localhost Host가 전달된 Vite 프록시 요청을 생성한다
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/oauth/local-login");
        // 원래 브라우저 요청이 localhost Vite에서 시작했음을 설정한다
        request.addHeader("X-Forwarded-Host", "localhost:5173");
        // 인증 Cookie와 이동 주소를 확인할 응답 객체를 생성한다
        MockHttpServletResponse response = new MockHttpServletResponse();

        // localhost 환경에서 활성 회원 번호로 간편 로그인을 요청한다
        localAuthLoginController.getLocalLogin(101L, request, response);

        // localhost Cookie를 사용할 수 있는 같은 호스트의 OAuth 완료 화면으로 이동하는지 확인한다
        assertEquals("http://localhost:5173/oauth", response.getRedirectedUrl());
        // 응답에 Access Token과 Refresh Token Cookie가 모두 포함되는지 확인한다
        assertTokenCookies(response.getHeaders(HttpHeaders.SET_COOKIE));
    }

    /**
     * Tailnet Host는 설정된 Tailnet 화면으로 복귀하고 인증 Cookie를 저장한다
     *
     * @author HanWon.Jang
     * @throws Exception 로그인 완료 리다이렉트 응답 기록에 실패할 때 발생
     */
    @Test
    void tailnetRedirectsTailnet() throws Exception {
        // 활성 회원의 개발용 로그인 성공 결과를 구성한다
        when(localAuthService.setLocalLogin(101L)).thenReturn(getLoginResult());
        // changeOrigin 이전의 Tailnet Host가 전달된 Vite 프록시 요청을 생성한다
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/oauth/local-login");
        // 원래 브라우저 요청이 Tailnet Vite에서 시작했음을 설정한다
        request.addHeader("X-Forwarded-Host", "device.tailnet.example");
        // 인증 Cookie와 이동 주소를 확인할 응답 객체를 생성한다
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Tailnet 환경에서 활성 회원 번호로 간편 로그인을 요청한다
        localAuthLoginController.getLocalLogin(101L, request, response);

        // Tailnet Cookie를 사용할 수 있는 설정 주소의 OAuth 완료 화면으로 이동하는지 확인한다
        assertEquals("https://device.tailnet.example/oauth", response.getRedirectedUrl());
        // 응답에 Access Token과 Refresh Token Cookie가 모두 포함되는지 확인한다
        assertTokenCookies(response.getHeaders(HttpHeaders.SET_COOKIE));
    }

    /**
     * localhost와 설정된 Tailnet 외의 Host는 회원 조회와 인증 세션 생성을 시작하지 않는다
     *
     * @author HanWon.Jang
     * @throws Exception 로그인 화면 리다이렉트 응답 기록에 실패할 때 발생
     */
    @Test
    void unknownHostDoesNotLogin() throws Exception {
        // 허용 목록에 없는 LAN Host가 전달된 개발 서버 요청을 생성한다
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/oauth/local-login");
        // localhost와 설정된 Tailnet 주소가 아닌 원래 브라우저 Host를 설정한다
        request.addHeader("X-Forwarded-Host", "192.0.2.10:5173");
        // 로그인 차단 이동 주소를 확인할 응답 객체를 생성한다
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 허용하지 않은 Host에서 간편 로그인을 요청한다
        localAuthLoginController.getLocalLogin(101L, request, response);

        // 임의 Host를 리다이렉트에 사용하지 않고 설정된 로그인 화면으로 이동하는지 확인한다
        assertEquals("https://device.tailnet.example/login", response.getRedirectedUrl());
        // 허용하지 않은 Host 요청은 회원 조회와 JWT 발급을 시작하지 않는지 확인한다
        verifyNoInteractions(localAuthService);
    }

    /**
     * 컨트롤러 Cookie 발급 검증에 사용할 로그인 성공 결과를 생성한다
     *
     * @author HanWon.Jang
     * @return Access Token과 Refresh Token을 포함한 성공 결과
     */
    private ResultData getLoginResult() {
        // 브라우저 인증 Cookie로 변환할 테스트 토큰 DTO를 생성한다
        TokenDto token = TokenDto.of("access-token", "refresh-token", false);
        // 발급 토큰을 포함한 공통 성공 결과를 반환한다
        return ResultData.success(token);
    }

    /**
     * 로그인 응답에 두 인증 Cookie와 HttpOnly 속성이 포함되는지 확인한다
     *
     * @author HanWon.Jang
     * @param setCookieHeaders 로그인 응답의 Set-Cookie Header 목록
     */
    private void assertTokenCookies(Collection<String> setCookieHeaders) {
        // Access Token Cookie가 JavaScript 읽기 제한과 함께 발급되었는지 확인한다
        assertTrue(setCookieHeaders.stream().anyMatch(
                header -> header.startsWith("accessToken=access-token") && header.contains("HttpOnly")));
        // Refresh Token Cookie가 JavaScript 읽기 제한과 함께 발급되었는지 확인한다
        assertTrue(setCookieHeaders.stream().anyMatch(
                header -> header.startsWith("refreshToken=refresh-token") && header.contains("HttpOnly")));
    }
}
