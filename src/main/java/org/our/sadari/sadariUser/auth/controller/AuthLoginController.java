package org.our.sadari.sadariUser.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.sadariUser.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kakao OAuth 로그인, JWT 재발급, logout API를 제공하는 컨트롤러입니다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthLoginController {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final long ACCESS_TOKEN_COOKIE_MAX_AGE_SECONDS = 60 * 60;
    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 7;

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;

    @Value("${domain.front}")
    private String frontDomain;

    /**
     * accessToken 쿠키를 기준으로 현재 로그인 상태를 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param request accessToken 쿠키가 포함된 HTTP 요청
     * @return 인증 상태 확인 결과
     */
    @GetMapping("/tokenCheck")
    public ResultData tokenCheck(HttpServletRequest request) {
        String accessToken = extractAccessToken(request);

        // accessToken 쿠키가 없으면 인증 정보를 구성할 수 없으므로 실패로 응답합니다.
        if (StringUtil.isEmpty(accessToken)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // JWT 서명과 만료 시간이 유효하지 않으면 재로그인이 필요합니다.
        if (!jwtProvider.validateToken(accessToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // logout 처리된 accessToken은 Redis blacklist에 등록되므로 유효한 JWT라도 거부합니다.
        if (tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(accessToken))) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        return ResultData.success();
    }

    /**
     * Kakao OAuth callback 인가 코드로 서비스 로그인 처리를 완료합니다.
     *
     * @author Seunghyeon.Kang
     * @param code Kakao에서 전달한 OAuth 인가 코드
     * @param request 로그인 이력 저장을 위한 IP와 User-Agent를 포함한 요청
     * @param response JWT 쿠키와 redirect 응답을 작성할 응답 객체
     * @throws Exception Kakao 인증 또는 redirect 처리 중 오류가 발생한 경우
     */
    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin(@RequestParam("code") String code
                                , HttpServletRequest request
                                , HttpServletResponse response) throws Exception {

        TokenDto token = authService.kakaoLogin(code, getLoginIp(request), getUserAgent(request));

        // 로그인 성공 후 accessToken과 refreshToken은 JavaScript에서 읽지 못하도록 HttpOnly 쿠키로 내려줍니다.
        addTokenCookies(response, token.getAccessToken(), token.getRefreshToken());
        response.sendRedirect(frontDomain + "/oauth");
    }

    /**
     * refreshToken 쿠키를 검증하고 accessToken과 refreshToken을 새로 발급합니다.
     *
     * @author Seunghyeon.Kang
     * @param request refreshToken 쿠키가 포함된 HTTP 요청
     * @param response 새 토큰 쿠키 또는 만료 쿠키를 작성할 응답 객체
     * @return 토큰 재발급 처리 결과
     */
    @PostMapping("/refresh")
    public ResultData refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        // refreshToken이 없거나 JWT 검증에 실패하면 브라우저에 남아 있는 토큰 쿠키를 모두 만료시킵니다.
        if (StringUtil.isEmpty(refreshToken) || !jwtProvider.validateToken(refreshToken)) {
            expireTokenCookies(response);
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        Long userNumb = jwtProvider.getUserNumb(refreshToken);
        String savedRefreshToken = tokenRedisService.getRefreshToken(userNumb);

        // Redis에 저장된 refreshToken과 다르면 이미 rotation 되었거나 logout된 토큰으로 판단합니다.
        if (StringUtil.isEmpty(savedRefreshToken) || !savedRefreshToken.equals(refreshToken)) {
            expireTokenCookies(response);
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        String newAccessToken = jwtProvider.createAccessToken(userNumb, AuthConstant.ROLE_USER);
        String newRefreshToken = jwtProvider.createRefreshToken(userNumb);

        // refreshToken rotation 정책에 따라 새 refreshToken만 Redis에 남기고 이전 토큰은 자연스럽게 무효화합니다.
        tokenRedisService.setRefreshToken(
                userNumb,
                newRefreshToken,
                jwtProvider.getRefreshTokenValiditySeconds()
        );

        addTokenCookies(response, newAccessToken, newRefreshToken);
        return ResultData.success();
    }

    /**
     * 현재 브라우저의 accessToken과 refreshToken을 logout 처리합니다.
     *
     * @author Seunghyeon.Kang
     * @param request 토큰 쿠키가 포함된 HTTP 요청
     * @param response 만료 쿠키를 작성할 응답 객체
     * @return logout 처리 결과
     */
    @PostMapping("/logout")
    public ResultData logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = extractAccessToken(request);
        String refreshToken = extractRefreshToken(request);

        // accessToken은 stateless 특성이 있으므로 남은 만료 시간 동안 Redis blacklist에 등록해 즉시 차단합니다.
        if (!StringUtil.isEmpty(accessToken) && jwtProvider.validateToken(accessToken)) {
            tokenRedisService.setAccessTokenBlacklist(
                    jwtProvider.getTokenId(accessToken),
                    jwtProvider.getRemainingSeconds(accessToken)
            );
        }

        // refreshToken을 Redis에서 제거하면 이후 accessToken 재발급 경로가 차단됩니다.
        if (!StringUtil.isEmpty(refreshToken) && jwtProvider.validateToken(refreshToken)) {
            tokenRedisService.deleteRefreshToken(jwtProvider.getUserNumb(refreshToken));
        }

        expireTokenCookies(response);
        return ResultData.success();
    }

    /**
     * accessToken과 refreshToken 쿠키를 응답에 추가합니다.
     *
     * @author Seunghyeon.Kang
     * @param response 쿠키를 추가할 HTTP 응답
     * @param accessToken accessToken 값
     * @param refreshToken refreshToken 값
     */
    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, createAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString());
    }

    /**
     * 브라우저에 저장된 토큰 쿠키를 만료시킵니다.
     *
     * @author Seunghyeon.Kang
     * @param response 만료 쿠키를 추가할 HTTP 응답
     */
    private void expireTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(ACCESS_TOKEN_COOKIE_NAME).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(REFRESH_TOKEN_COOKIE_NAME).toString());
    }

    private ResponseCookie createAccessTokenCookie(String accessToken) {
        return createTokenCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessToken,
                ACCESS_TOKEN_COOKIE_MAX_AGE_SECONDS
        );
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createTokenCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS
        );
    }

    private ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie createExpiredCookie(String name) {
        return createTokenCookie(name, "", 0);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    private String extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    private String extractCookieValue(HttpServletRequest request, String name) {
        // 쿠키 배열 자체가 없을 수 있으므로 반복 전에 비어 있는지 확인합니다.
        if (StringUtil.isEmpty(request.getCookies())) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * 로그인 이력에 저장할 클라이언트 IP를 계산합니다.
     *
     * @author Seunghyeon.Kang
     * @param request 로그인 callback 요청
     * @return 로그인 이력에 저장할 IP 주소
     */
    private String getLoginIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        // 프록시 환경에서는 X-Forwarded-For의 첫 번째 값이 최초 클라이언트 IP입니다.
        if (!StringUtil.isEmpty(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (!StringUtil.isEmpty(realIp)) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 로그인 이력에 저장할 User-Agent 값을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param request 로그인 callback 요청
     * @return User-Agent 헤더 값
     */
    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}
