package org.our.sadari.user.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.push.service.PushService;
import org.our.sadari.user.auth.provider.KakaoAuthProvider;
import org.our.sadari.user.auth.service.AuthService;
import org.our.sadari.user.mapper.UserMapper;
import org.our.sadari.user.service.UserWithdrawalService;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : AuthLoginControllerTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-24
 * description    : OAuth 상태값 결속과 실패 콜백의 기존 세션 보존을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-24        SeungHyeon.Kang    OAuth 로그인 CSRF 및 세션 보존 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class AuthLoginControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private KakaoAuthProvider kakaoAuthProvider;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private TokenRedisService tokenRedisService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserWithdrawalService userWithdrawalService;
    @Mock
    private FileService fileService;
    @Mock
    private PushService pushService;

    // OAuth 로그인 컨트롤러 단위 테스트 대상
    private AuthLoginController authLoginController;

    /** 각 테스트에서 OAuth 컨트롤러와 쿠키 환경 설정을 구성한다. */
    @BeforeEach
    void setUp() {
        // 인증 흐름의 모든 의존 객체를 대역으로 연결한다
        authLoginController = new AuthLoginController(
                authService
              , kakaoAuthProvider
              , jwtProvider
              , tokenRedisService
              , userMapper
              , userWithdrawalService
              , fileService
              , pushService
        );
        // 콜백 리다이렉트와 로컬 테스트 쿠키에 사용할 설정값을 주입한다
        ReflectionTestUtils.setField(authLoginController, "frontDomain", "https://front.example");
        ReflectionTestUtils.setField(authLoginController, "cookieSecure", false);
        ReflectionTestUtils.setField(authLoginController, "cookieSameSite", "Lax");
    }

    /** 로그인 시작 시 동일한 일회성 state가 HttpOnly 쿠키와 Kakao URL에 포함된다. */
    @Test
    void loginStartSetsState() throws Exception {
        // Provider가 전달받은 상태값을 포함한 테스트 인가 URL을 반환하도록 구성한다
        when(kakaoAuthProvider.getKakaoLoginUrl(anyString()))
                .thenAnswer(invocation -> "https://kakao.example/authorize?state=" + invocation.getArgument(0));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 일반 Kakao 로그인을 시작한다
        authLoginController.getKakaoAuthorization(response);

        // Provider에 전달된 일회성 상태값을 조회한다
        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        verify(kakaoAuthProvider).getKakaoLoginUrl(stateCaptor.capture());
        String state = stateCaptor.getValue();
        String stateCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        // 추측하기 어려운 로그인 전용 상태값과 HttpOnly 제한 쿠키를 확인한다
        assertTrue(state.startsWith("login_"));
        assertNotNull(stateCookie);
        assertTrue(stateCookie.contains("oauthLoginState=" + state));
        assertTrue(stateCookie.contains("HttpOnly"));
        assertTrue(stateCookie.contains("Path=/api/oauth/callback/kakao"));
        // Kakao 인가 URL에도 쿠키와 같은 상태값이 전달되는지 확인한다
        assertEquals("https://kakao.example/authorize?state=" + state, response.getRedirectedUrl());
    }

    /** 일치하지 않는 로그인 state 콜백은 로그인 처리와 기존 인증 쿠키를 변경하지 않는다. */
    @Test
    void invalidStateKeepsSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("oauthLoginState", "login_expected")
              , new Cookie("accessToken", "existing-access")
              , new Cookie("refreshToken", "existing-refresh")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 브라우저 쿠키와 다른 상태값의 콜백을 전달한다
        authLoginController.kakaoAuthLogin("untrusted-code", "login_attacker", request, response);

        // 검증되지 않은 인가 코드가 로그인 또는 탈퇴 서비스에 전달되지 않는지 확인한다
        verifyNoInteractions(authService, userWithdrawalService);
        // 실패 콜백이 기존 Access/Refresh 쿠키를 만료시키지 않는지 확인한다
        assertNoAuthCookieChange(response.getHeaders(HttpHeaders.SET_COOKIE));
        assertEquals("https://front.example/oauth", response.getRedirectedUrl());
    }

    /** 탈퇴 재인증 처리 실패 시에도 기존 인증 쿠키를 유지한다. */
    @Test
    void withdrawalKeepsSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResultData withdrawalResult = org.mockito.Mockito.mock(ResultData.class);
        // 탈퇴 상태값 검증 또는 계정 확인에 실패한 결과를 구성한다
        when(withdrawalResult.getCode()).thenReturn(400);
        when(userWithdrawalService.setWithdrawalCallback("code", "withdrawal-state"))
                .thenReturn(withdrawalResult);

        // 실패하는 탈퇴 재인증 콜백을 실행한다
        authLoginController.kakaoAuthLogin("code", "withdrawal-state", request, response);

        // 탈퇴 서비스 호출 뒤에도 인증 쿠키 만료 응답이 없는지 확인한다
        verify(userWithdrawalService).setWithdrawalCallback("code", "withdrawal-state");
        assertNoAuthCookieChange(response.getHeaders(HttpHeaders.SET_COOKIE));
        assertEquals("https://front.example/withdrawal/result?success=N", response.getRedirectedUrl());
    }

    /** 인증 쿠키를 추가하거나 만료하는 Set-Cookie 응답이 없는지 확인한다. */
    private void assertNoAuthCookieChange(Collection<String> setCookieHeaders) {
        // 기존 로그인 세션을 나타내는 두 쿠키명이 응답에 포함되지 않았는지 확인한다
        assertTrue(setCookieHeaders.stream().noneMatch(header -> header.startsWith("accessToken=")));
        assertTrue(setCookieHeaders.stream().noneMatch(header -> header.startsWith("refreshToken=")));
    }
}
