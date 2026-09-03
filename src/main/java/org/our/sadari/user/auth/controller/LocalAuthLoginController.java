package org.our.sadari.user.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.user.auth.service.LocalAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : LocalAuthLoginController
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 로컬 프로필의 localhost와 Tailnet 개발용 간편 로그인 URL을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 */
@RestController
@Profile("loc & !prod")
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Tag(name = "로컬 인증", description = "로컬 프로필에서 활성 회원의 개발용 로그인 세션을 발급한다")
public class LocalAuthLoginController {

    // 접근 TOKEN COOKIE 명칭
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    // REFRESH TOKEN COOKIE 명칭
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    // Vite 개발 프록시가 전달하는 원래 브라우저 Host Header 명칭
    private static final String FORWARDED_HOST_HEADER_NAME = "X-Forwarded-Host";
    // localhost 요청을 구분할 표준 호스트명
    private static final String LOCALHOST_NAME = "localhost";

    // 로컬 개발용 로그인 세션 발급 서비스
    private final LocalAuthService localAuthService;

    // Tailnet 요청의 로그인 완료 후 이동할 프런트엔드 주소
    @Value("${domain.front}")
    private String frontDomain;
    // localhost 요청의 로그인 완료 후 이동할 프런트엔드 주소
    @Value("${domain.local-front}")
    private String localFrontDomain;
    // Access Token Cookie 유지 시간
    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenCookieMaxAgeSeconds;
    // Refresh Token Cookie 유지 시간
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenCookieMaxAgeSeconds;
    // HTTPS에서만 인증 Cookie를 전송할지 여부
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;
    // 인증 Cookie의 SameSite 정책
    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    /**
     * URL의 회원 번호로 로컬 개발용 로그인 쿠키를 발급하고 같은 접속 환경의 프런트엔드로 이동한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인할 회원 번호
     * @param request localhost와 Tailnet 접속 환경을 판정할 HTTP 요청
     * @param response 인증 쿠키와 이동 주소를 기록할 HTTP 응답
     * @throws IOException 로그인 완료 화면 이동 응답 기록에 실패할 때 발생
     */
    @GetMapping("/local-login")
    @Operation(summary = "로컬 개발용 간편 로그인", description = "loc 프로필에서 활성 회원 번호로 인증 쿠키를 발급하고 OAuth 완료 화면으로 이동한다.")
    public void getLocalLogin(@Parameter(description = "로그인할 회원 번호", example = "31") @RequestParam Long userNumb
                            , @Parameter(hidden = true) HttpServletRequest request
                            , @Parameter(hidden = true) HttpServletResponse response) throws IOException {
        // Vite 프록시가 전달한 원래 Host를 기준으로 localhost와 Tailnet 이동 주소를 선택한다
        String redirectDomain = getRedirectDomain(request);

        // localhost와 설정된 Tailnet 호스트가 아니면 회원 조회 전에 간편 로그인 요청을 차단한다
        if (StringUtil.isEmpty(redirectDomain)) {
            // 허용하지 않은 Host 요청은 설정된 로그인 화면으로만 이동시킨다
            response.sendRedirect(frontDomain + "/login");
            // 검증되지 않은 Host에서 인증 세션을 생성하지 않도록 처리를 종료한다
            return;
        }

        // DB 원본 상태와 권한을 검증한 회원의 개발용 로그인 세션을 발급한다
        ResultData loginResult = localAuthService.setLocalLogin(userNumb);

        // 조회 실패 또는 활성 상태가 아닌 계정은 기존 인증 쿠키를 변경하지 않고 로그인 화면으로 이동시킨다
        if (loginResult.getCode() != 200 || !(loginResult.getData() instanceof TokenDto token)) {
            // 인증 실패 결과를 같은 접속 환경의 로그인 화면으로 전달한다
            response.sendRedirect(redirectDomain + "/login");
            // 실패한 회원 번호로 인증 쿠키가 생성되지 않도록 처리를 종료한다
            return;
        }

        // 검증된 회원에게 발급한 Access Token과 Refresh Token을 HttpOnly Cookie로 저장한다
        addTokenCookies(response, token.getAccessToken(), token.getRefreshToken());
        // localhost 또는 Tailnet 중 요청을 시작한 환경의 OAuth 완료 화면으로 이동시킨다
        response.sendRedirect(redirectDomain + "/oauth");
    }

    /**
     * Vite 개발 프록시의 원래 Host를 기준으로 로그인 완료 프런트엔드 주소를 선택한다
     *
     * @author HanWon.Jang
     * @param request 브라우저 Host와 전달 Host를 포함한 HTTP 요청
     * @return localhost 또는 Tailnet 프런트엔드 주소
     */
    private String getRedirectDomain(HttpServletRequest request) {
        // Vite가 백엔드에 전달한 브라우저의 원래 Host를 조회한다
        String forwardedHost = request.getHeader(FORWARDED_HOST_HEADER_NAME);
        String requestHost;

        // 프록시를 거치지 않은 직접 요청은 Servlet이 해석한 Host를 사용한다
        if (StringUtil.isEmpty(forwardedHost)) {
            // 직접 localhost 또는 Tailnet 요청의 호스트명을 조회한다
            requestHost = request.getServerName();
        }

        // Vite 프록시 요청은 changeOrigin으로 바뀌기 전의 브라우저 Host를 사용한다
        else {
            requestHost = forwardedHost;
        }

        // 대소문자와 포트 유무에 관계없이 localhost 요청을 판정할 값을 정규화한다
        String normalizedHost = requestHost.toLowerCase(Locale.ROOT);

        // localhost 또는 localhost 포트 요청은 로컬 Vite 주소로 복귀시킨다
        if (LOCALHOST_NAME.equals(normalizedHost) || normalizedHost.startsWith(LOCALHOST_NAME + ":")) {
            // localhost에서 발급된 Host 전용 Cookie를 같은 localhost 화면에서 사용하도록 반환한다
            return localFrontDomain;
        }

        // 환경 설정 URL에서 간편 로그인을 허용할 Tailnet 호스트명을 추출한다
        String configuredFrontHost = URI.create(frontDomain).getHost();

        // 설정된 Tailnet 호스트와 해당 호스트의 명시적 포트 요청만 허용한다
        if (!StringUtil.isEmpty(configuredFrontHost)
                && (configuredFrontHost.equalsIgnoreCase(normalizedHost)
                || normalizedHost.startsWith(configuredFrontHost.toLowerCase(Locale.ROOT) + ":"))) {
            // Tailnet Host 전용 Cookie를 설정된 Tailnet 화면에서 사용하도록 반환한다
            return frontDomain;
        }

        // localhost와 설정된 Tailnet 호스트가 아닌 요청은 로그인 처리 전에 차단하도록 빈 주소를 반환한다
        return null;
    }

    /**
     * 개발용 Access Token과 Refresh Token을 브라우저 인증 Cookie로 저장한다
     *
     * @author HanWon.Jang
     * @param response 인증 Cookie를 기록할 HTTP 응답
     * @param accessToken API 인증에 사용할 Access Token
     * @param refreshToken Access Token 재발급에 사용할 Refresh Token
     */
    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token을 JavaScript에서 읽을 수 없는 인증 Cookie로 저장한다
        response.addHeader(HttpHeaders.SET_COOKIE, createTokenCookie(
                ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenCookieMaxAgeSeconds).toString());
        // Refresh Token을 JavaScript에서 읽을 수 없는 인증 Cookie로 저장한다
        response.addHeader(HttpHeaders.SET_COOKIE, createTokenCookie(
                REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTokenCookieMaxAgeSeconds).toString());
    }

    /**
     * 로컬 프로필의 인증 환경 설정을 적용한 HttpOnly Cookie를 생성한다
     *
     * @author HanWon.Jang
     * @param name 생성할 Cookie 이름
     * @param value 생성할 Cookie 값
     * @param maxAgeSeconds Cookie 유지 시간
     * @return 인증 환경 설정이 적용된 Cookie
     */
    private ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
        // 기존 OAuth 로그인과 같은 속성을 적용한 인증 Cookie를 반환한다
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .sameSite(cookieSameSite)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
