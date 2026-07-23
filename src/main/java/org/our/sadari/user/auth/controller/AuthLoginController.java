package org.our.sadari.user.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.auth.service.AuthService;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthLoginController 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
@Tag(name = "인증", description = "카카오 OAuth 로그인, JWT 검증, 재발급, 로그아웃 API")
public class AuthLoginController {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;
    private final UserMapper userMapper;

    @Value("${domain.front}")
    private String frontDomain;

    // Cookie Max-Age는 초 단위를 받으므로 yml의 JWT 유효시간 값을 1000 곱하지 않고 그대로 사용한다.
    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenCookieMaxAgeSeconds;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenCookieMaxAgeSeconds;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    /**
     * tokenCheck 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @return 처리 결과
     */
    @GetMapping("/tokenCheck")
    @Operation(summary = "Access Token 검증", description = "HttpOnly 쿠키의 Access Token 유효성 및 로그아웃 블랙리스트 여부를 검증한다.")
    public ResultData tokenCheck(@Parameter(hidden = true) HttpServletRequest request) {
        String accessToken = extractAccessToken(request);

        // 요청 쿠키에 Access Token이 없으면 인증 실패 응답을 반환한다.
        if (StringUtil.isEmpty(accessToken)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // Access Token의 위변조 여부 및 만료 시간을 검증하여 유효하지 않으면 실패 처리한다.
        if (!jwtProvider.validateToken(accessToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 로그아웃되어 Redis 블랙리스트에 등록된 Access Token(jti 기준)인지 확인한다.
        if (tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(accessToken))) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        return ResultData.success();
    }

    /**
     * kakaoAuthLogin 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param code 인가 코드
     * @param request 처리에 필요한 입력값
     * @param response 처리에 필요한 입력값
     */
    @GetMapping("/callback/kakao")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인가 코드를 받아 서비스 토큰을 발급하고 프론트 OAuth 처리 화면으로 리다이렉트한다.")
    public void kakaoAuthLogin(@Parameter(description = "카카오 OAuth 인가 코드")
                               @RequestParam("code") String code
            , @Parameter(hidden = true) HttpServletRequest request
            , @Parameter(hidden = true) HttpServletResponse response) throws Exception {

        ResultData loginResult = authService.kakaoLogin(code, getLoginIp(request), getUserAgent(request));

        // 카카오 로그인 서비스 처리 실패 시 기존 토큰 쿠키를 만료시키고 로그인 페이지로 리다이렉트한다.
        if (loginResult.getCode() != 200) {
            expireTokenCookies(response);
            response.sendRedirect(frontDomain + "/oauth");
            return;
        }

        TokenDto token = (TokenDto) loginResult.getData();

        // 발급된 토큰을 HttpOnly 쿠키에 담아 응답 헤더에 추가하고 프론트엔드로 리다이렉트한다.
        addTokenCookies(response, token.getAccessToken(), token.getRefreshToken());
        response.sendRedirect(frontDomain + "/oauth");
    }

    /**
     * OAuth 콜백 루트 URL로 직접 접근했을 때 보여줄 HTML 오류 화면을 반환한다.
     * 실제 로그인 처리는 /api/oauth/callback/kakao에서만 수행하므로, 잘못된 콜백 URL에서는 ResultData JSON이 브라우저에 그대로 노출되지 않도록 분리한다.
     *
     * @author Seunghyeon.Kang
     * @return OAuth 콜백 오류 안내 HTML
     */
    @GetMapping(value = {"/callback", "/callback/"}, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "OAuth 콜백 오류 화면", description = "지원하지 않는 OAuth 콜백 루트 접근 시 브라우저용 오류 화면을 반환한다.")
    public ResponseEntity<String> oauthCallbackErrorPage() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_HTML)
                .body(createOauthCallbackErrorHtml());
    }

    /**
     * refresh 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @param response 처리에 필요한 입력값
     * @return 처리 결과
     */
    @PostMapping("/refresh")
    @Operation(summary = "JWT 재발급", description = "Refresh Token 쿠키를 검증하고 Access Token과 Refresh Token을 재발급한다.")
    public ResultData refresh(@Parameter(hidden = true) HttpServletRequest request,
                              @Parameter(hidden = true) HttpServletResponse response) {

        String refreshToken = extractRefreshToken(request);

        // Refresh Token의 존재 여부 및 위변조/만료 상태를 검증한다.
        if (StringUtil.isEmpty(refreshToken) || !jwtProvider.validateToken(refreshToken)) {
            expireTokenCookies(response);
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        Long userNumb = jwtProvider.getUserNumb(refreshToken);
        String savedRefreshToken = tokenRedisService.getRefreshToken(userNumb);

        // Redis에 저장된 Refresh Token과 전달받은 쿠키의 Refresh Token이 일치하는지 비교 검증한다.
        if (StringUtil.isEmpty(savedRefreshToken) || !savedRefreshToken.equals(refreshToken)) {
            expireTokenCookies(response);
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        UserDto savedUser = userMapper.getUserByNumb(userNumb);

        // Access Token 재발급 시에도 DB에 저장된 현재 권한을 사용해야 ADMIN 사용자가 Swagger 접근 권한을 유지할 수 있다.
        if (StringUtil.isEmpty(savedUser)) {
            expireTokenCookies(response);
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        String newAccessToken = jwtProvider.createAccessToken(userNumb, savedUser.getUserRole());
        String newRefreshToken = jwtProvider.createRefreshToken(userNumb);

        // 신규 발급된 Refresh Token을 Redis 저장소에 저장(RTR 패턴)하고, 쿠키에도 새로 발급된 토큰들을 내려준다.
        tokenRedisService.setRefreshToken(
                userNumb,
                newRefreshToken,
                jwtProvider.getRefreshTokenValiditySeconds()
        );

        addTokenCookies(response, newAccessToken, newRefreshToken);
        return ResultData.success();
    }

    /**
     * logout 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @param response 처리에 필요한 입력값
     * @return 처리 결과
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Access Token을 Redis 블랙리스트에 등록하고 Refresh Token을 제거한 뒤 토큰 쿠키를 만료시킨다.")
    public ResultData logout(@Parameter(hidden = true) HttpServletRequest request,
                             @Parameter(hidden = true) HttpServletResponse response) {
        String accessToken = extractAccessToken(request);
        String refreshToken = extractRefreshToken(request);

        // 유효한 Access Token인 경우 남은 유효시간 동안 재사용하지 못하도록 jti를 Redis 블랙리스트에 등록한다.
        if (!StringUtil.isEmpty(accessToken) && jwtProvider.validateToken(accessToken)) {
            tokenRedisService.setAccessTokenBlacklist(
                    jwtProvider.getTokenId(accessToken),
                    jwtProvider.getRemainingSeconds(accessToken)
            );
        }

        // 유효한 Refresh Token인 경우 재발급에 사용되지 못하도록 Redis에서 제거한다.
        if (!StringUtil.isEmpty(refreshToken) && jwtProvider.validateToken(refreshToken)) {
            tokenRedisService.deleteRefreshToken(jwtProvider.getUserNumb(refreshToken));
        }

        // 브라우저의 토큰 쿠키를 삭제(만료 처리)한다.
        expireTokenCookies(response);
        return ResultData.success();
    }

    /**
     * addTokenCookies 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param response 처리에 필요한 입력값
     * @param accessToken 처리에 필요한 입력값
     * @param refreshToken 처리에 필요한 입력값
     */
    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, createAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString());
    }

    /**
     * OAuth 콜백 오류 화면의 HTML 문자열을 생성한다.
     * API 전용 URL을 사용자가 직접 열었을 때도 빈 화면이나 JSON 원문 대신 로그인 화면으로 돌아갈 수 있는 안내 화면을 제공한다.
     *
     * @author Seunghyeon.Kang
     * @return OAuth 콜백 오류 화면 HTML
     */
    private String createOauthCallbackErrorHtml() {
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>로그인 요청을 처리할 수 없어요</title>
                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: grid;
                            place-items: center;
                            padding: 24px;
                            background: #f5f7fb;
                            color: #191919;
                            font-family: Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                        }

                        .page {
                            width: min(100%, 420px);
                            padding: 34px 28px 30px;
                            border: 1px solid #e8edf5;
                            border-radius: 18px;
                            background: #ffffff;
                            text-align: center;
                            box-shadow: 0 18px 50px rgba(36, 56, 96, 0.12);
                        }

                        .mark {
                            width: 54px;
                            height: 54px;
                            display: grid;
                            place-items: center;
                            margin: 0 auto 18px;
                            border-radius: 18px;
                            background: #e8f3ff;
                            color: #2f80ed;
                            font-size: 28px;
                            font-weight: 800;
                        }

                        h1 {
                            margin: 0;
                            font-size: 22px;
                            line-height: 1.35;
                            letter-spacing: 0;
                        }

                        p {
                            margin: 12px 0 0;
                            color: #687386;
                            font-size: 14px;
                            line-height: 1.65;
                        }

                        a {
                            display: inline-flex;
                            align-items: center;
                            justify-content: center;
                            width: 100%;
                            height: 46px;
                            margin-top: 24px;
                            border-radius: 12px;
                            background: #2f80ed;
                            color: #ffffff;
                            font-size: 15px;
                            font-weight: 700;
                            text-decoration: none;
                        }
                    </style>
                </head>
                <body>
                    <main class="page">
                        <div class="mark">!</div>
                        <h1>로그인 요청을 처리할 수 없어요</h1>
                        <p>로그인 제공자 정보가 없는 콜백 주소로 접근했어요.<br>다시 로그인 화면에서 시작해주세요.</p>
                        <a href="%s/login">로그인 화면으로 돌아가기</a>
                    </main>
                </body>
                </html>
                """.formatted(frontDomain);
    }

    /**
     * expireTokenCookies 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param response 처리에 필요한 입력값
     */
    private void expireTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(ACCESS_TOKEN_COOKIE_NAME).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(REFRESH_TOKEN_COOKIE_NAME).toString());
    }

    /**
     * AccessToken 쿠키를 생성한다.
     *
     * @param accessToken 발급된 AccessToken
     * @return 생성된 ResponseCookie 객체
     */
    private ResponseCookie createAccessTokenCookie(String accessToken) {
        return createTokenCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessToken,
                accessTokenCookieMaxAgeSeconds
        );
    }

    /**
     * RefreshToken 쿠키를 생성한다.
     *
     * @param refreshToken 발급된 RefreshToken
     * @return 생성된 ResponseCookie 객체
     */
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createTokenCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                refreshTokenCookieMaxAgeSeconds
        );
    }

    /**
     * 공통 토큰 쿠키 객체를 생성한다.
     *
     * @param name 쿠키명
     * @param value 쿠키값
     * @param maxAgeSeconds 유효기간(초)
     * @return 생성된 ResponseCookie 객체
     */
    private ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                // 운영 HTTPS와 프론트/백 도메인 분리 여부에 따라 SameSite 값을 yml 환경변수로 조정한다.
                .sameSite(cookieSameSite)
                // 운영 HTTPS 배포에서는 true로 설정해 브라우저가 보안 연결에서만 토큰 쿠키를 전송하게 한다.
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    /**
     * 만료 처리용 빈 쿠키 객체를 생성한다.
     *
     * @param name 쿠키명
     * @return 만료 설정된 ResponseCookie 객체
     */
    private ResponseCookie createExpiredCookie(String name) {
        return createTokenCookie(name, "", 0);
    }

    /**
     * Request 쿠키에서 RefreshToken을 추출한다.
     *
     * @param request 처리에 필요한 입력값
     * @return 추출된 RefreshToken (없을 경우 null)
     */
    private String extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * Request 쿠키에서 AccessToken을 추출한다.
     *
     * @param request 처리에 필요한 입력값
     * @return 추출된 AccessToken (없을 경우 null)
     */
    private String extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    /**
     * Request 쿠키 목록에서 특정 이름의 쿠키 값을 추출한다.
     *
     * @param request 처리에 필요한 입력값
     * @param name 쿠키명
     * @return 쿠키 값 (없을 경우 null)
     */
    private String extractCookieValue(HttpServletRequest request, String name) {
        // 요청 헤더에 쿠키가 존재하지 않는 경우 null을 반환한다.
        if (StringUtil.isEmpty(request.getCookies())) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            // 찾고자 하는 쿠키명과 일치하는 쿠키가 존재하면 해당 값을 반환한다.
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @return 클라이언트 IP 주소
     */
    private String getLoginIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        // 프록시/로드밸런서를 거쳐 들어온 경우 원본 클라이언트 IP(X-Forwarded-For)를 우선 추출한다.
        if (!StringUtil.isEmpty(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        // Nginx 등에서 설정한 X-Real-IP 헤더가 존재하는 경우 해당 IP를 반환한다.
        if (!StringUtil.isEmpty(realIp)) {
            return realIp;
        }

        // 헤더 정보가 없는 경우 기본 RemoteAddr 주소를 반환한다.
        return request.getRemoteAddr();
    }

    /**
     * Request 헤더에서 User-Agent(브라우저/디바이스 정보)를 추출한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @return User-Agent 문자열
     */
    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}
