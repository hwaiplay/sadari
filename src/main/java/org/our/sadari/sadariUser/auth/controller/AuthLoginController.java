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
 * fileName       : AuthLoginController
 * author         : Seunghyeon.Kang
 * date           : 2026-03-15
 * description    : 카카오 로그인 콜백, JWT 쿠키 발급, refreshToken 재발급, logout을 처리하는 컨트롤러
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        Seunghyeon.Kang    최초 생성
 * 2026-07-13        Seunghyeon.Kang    Redis 기반 JWT 인증 흐름과 TB_LOGHIS 로그인 이력 저장 정보 추가
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
     * accessToken 쿠키를 기준으로 현재 브라우저의 로그인 상태를 확인한다.
     * JWT 서명과 만료 시간을 먼저 검증하고, Redis blacklist에 등록된 logout 토큰인지 추가로 확인한다.
     *
     * @Author Seunghyeon.Kang
     * @param request accessToken 쿠키가 포함될 수 있는 요청 객체
     * @return 인증 상태 확인 결과를 담은 공통 응답 객체
     */
    @GetMapping("/tokenCheck")
    public ResultData tokenCheck(HttpServletRequest request) {
        String accessToken = extractAccessToken(request);

        if (StringUtil.isEmpty(accessToken)) {
            // accessToken 쿠키가 없으면 현재 브라우저를 인증된 상태로 보지 않는다.
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        if (!jwtProvider.validateToken(accessToken)) {
            // JWT 자체가 위조되었거나 만료되었으면 더 이상 사용할 수 없는 토큰으로 처리한다.
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        if (tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(accessToken))) {
            // JWT가 유효하더라도 logout 시점에 blacklist에 들어간 accessToken은 인증 성공으로 처리하지 않는다.
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 쿠키 존재, JWT 검증, Redis blacklist 검증을 모두 통과하면 로그인 상태로 판단한다.
        return ResultData.success();
    }

    /**
     * 카카오 OAuth 인가 코드 콜백을 받아 서비스 로그인을 완료한다.
     * 카카오 사용자 조회 후 accessToken과 refreshToken을 발급하고 HttpOnly 쿠키로 내려준다.
     * 로그인 이력에는 IP와 User-Agent를 함께 저장하여 접속 환경을 사후 확인할 수 있게 한다.
     *
     * @Author Seunghyeon.Kang
     * @param code 카카오에서 전달한 OAuth 인가 코드
     * @param request 로그인 이력용 IP와 User-Agent를 추출할 요청 객체
     * @param response 토큰 쿠키 설정과 프론트 redirect에 사용할 응답 객체
     * @throws Exception 카카오 인증 처리 또는 redirect 처리 중 발생하는 예외
     */
    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin(@RequestParam("code") String code
                                , HttpServletRequest request
                                , HttpServletResponse response) throws Exception {

        TokenDto token = authService.kakaoLogin(code, getLoginIp(request), getUserAgent(request));

        // 카카오 로그인 성공 후 브라우저가 이후 요청에 사용할 서비스 JWT 쿠키를 내려준다.
        addTokenCookies(response, token.getAccessToken(), token.getRefreshToken());
        // 프론트 OAuth 완료 화면에서 인증 상태를 다시 확인하도록 redirect한다.
        response.sendRedirect(frontDomain + "/oauth");
    }

    /**
     * refreshToken 쿠키를 검증하고 accessToken을 재발급한다.
     * Redis에 저장된 refreshToken과 요청 쿠키 값을 비교하여 서버가 인정하는 토큰인지 확인한다.
     * 검증에 성공하면 refreshToken rotation 정책에 따라 accessToken과 refreshToken을 모두 새로 발급한다.
     *
     * @Author Seunghyeon.Kang
     * @param request refreshToken 쿠키가 포함될 수 있는 요청 객체
     * @param response 새 토큰 쿠키 또는 만료 쿠키를 내려줄 응답 객체
     * @return 토큰 재발급 처리 결과를 담은 공통 응답 객체
     */
    @PostMapping("/refresh")
    public ResultData refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        if (StringUtil.isEmpty(refreshToken) || !jwtProvider.validateToken(refreshToken)) {
            // refreshToken이 없거나 JWT 검증에 실패하면 브라우저 토큰을 모두 제거해 재시도를 막는다.
            expireTokenCookies(response);
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        Long userNumb = jwtProvider.getUserNumb(refreshToken);
        String savedRefreshToken = tokenRedisService.getRefreshToken(userNumb);

        if (StringUtil.isEmpty(savedRefreshToken) || !savedRefreshToken.equals(refreshToken)) {
            // Redis 값과 쿠키 값이 다르면 이미 logout되었거나 rotation 이후 폐기된 토큰으로 판단한다.
            expireTokenCookies(response);
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // refreshToken rotation을 적용하기 위해 accessToken과 refreshToken을 모두 새로 만든다.
        String newAccessToken = jwtProvider.createAccessToken(userNumb, AuthConstant.ROLE_USER);
        String newRefreshToken = jwtProvider.createRefreshToken(userNumb);

        // 새 refreshToken만 Redis에 남겨 이전 refreshToken 재사용을 차단한다.
        tokenRedisService.setRefreshToken(
                userNumb,
                newRefreshToken,
                jwtProvider.getRefreshTokenValiditySeconds()
        );

        // 브라우저 쿠키를 새 토큰으로 교체하여 다음 요청부터 새 JWT만 사용하게 한다.
        addTokenCookies(response, newAccessToken, newRefreshToken);
        return ResultData.success();
    }

    /**
     * 현재 브라우저의 서비스 로그인 상태를 logout 처리한다.
     * refreshToken은 Redis에서 제거하여 재발급 권한을 없애고, 남은 accessToken은 Redis blacklist에 등록한다.
     * 마지막으로 브라우저 쿠키를 만료시켜 이후 요청에 토큰이 실리지 않게 한다.
     *
     * @Author Seunghyeon.Kang
     * @param request accessToken과 refreshToken 쿠키가 포함될 수 있는 요청 객체
     * @param response 토큰 쿠키 제거 헤더를 내려줄 응답 객체
     * @return logout 처리 결과를 담은 공통 응답 객체
     */
    @PostMapping("/logout")
    public ResultData logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = extractAccessToken(request);
        String refreshToken = extractRefreshToken(request);

        if (!StringUtil.isEmpty(accessToken) && jwtProvider.validateToken(accessToken)) {
            // 아직 살아 있는 accessToken은 남은 만료 시간 동안 blacklist에 올려 logout 직후 즉시 차단한다.
            tokenRedisService.setAccessTokenBlacklist(
                    jwtProvider.getTokenId(accessToken),
                    jwtProvider.getRemainingSeconds(accessToken)
            );
        }

        if (!StringUtil.isEmpty(refreshToken) && jwtProvider.validateToken(refreshToken)) {
            // refreshToken을 제거하면 accessToken 재발급 경로가 차단되어 로그인 유지가 불가능해진다.
            tokenRedisService.deleteRefreshToken(jwtProvider.getUserNumb(refreshToken));
        }

        // HttpOnly 쿠키는 프론트에서 직접 지울 수 없으므로 서버 응답으로 만료 처리한다.
        expireTokenCookies(response);
        return ResultData.success();
    }

    /**
     * accessToken과 refreshToken을 응답 쿠키로 추가한다.
     * 두 토큰 모두 JavaScript에서 읽을 수 없도록 HttpOnly 쿠키로 생성한다.
     *
     * @Author Seunghyeon.Kang
     * @param response 토큰 쿠키를 추가할 응답 객체
     * @param accessToken 인증에 사용할 accessToken 문자열
     * @param refreshToken accessToken 재발급에 사용할 refreshToken 문자열
     */
    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, createAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString());
    }

    /**
     * 브라우저에 저장된 accessToken과 refreshToken 쿠키를 만료시킨다.
     * logout 처리와 refresh 실패 처리에서 공통으로 사용한다.
     *
     * @Author Seunghyeon.Kang
     * @param response 토큰 만료 쿠키를 추가할 응답 객체
     */
    private void expireTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(ACCESS_TOKEN_COOKIE_NAME).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(REFRESH_TOKEN_COOKIE_NAME).toString());
    }

    /**
     * accessToken 값을 담은 HttpOnly 쿠키를 생성한다.
     * accessToken은 인증에 직접 사용되므로 refreshToken보다 짧은 만료 시간을 적용한다.
     *
     * @Author Seunghyeon.Kang
     * @param accessToken 쿠키 값으로 내려줄 accessToken 문자열
     * @return accessToken 설정값이 반영된 응답 쿠키 객체
     */
    private ResponseCookie createAccessTokenCookie(String accessToken) {
        return createTokenCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessToken,
                ACCESS_TOKEN_COOKIE_MAX_AGE_SECONDS
        );
    }

    /**
     * refreshToken 값을 담은 HttpOnly 쿠키를 생성한다.
     * refreshToken은 accessToken 재발급에만 사용되며 Redis 저장값과 비교되는 서버 인정 토큰이다.
     *
     * @Author Seunghyeon.Kang
     * @param refreshToken 쿠키 값으로 내려줄 refreshToken 문자열
     * @return refreshToken 설정값이 반영된 응답 쿠키 객체
     */
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createTokenCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS
        );
    }

    /**
     * 지정한 이름, 값, 만료 시간으로 공통 토큰 쿠키를 생성한다.
     * 토큰 쿠키의 보안 속성을 한 곳에서 관리하기 위해 사용한다.
     *
     * @Author Seunghyeon.Kang
     * @param name 생성할 쿠키 이름
     * @param value 생성할 쿠키 값
     * @param maxAgeSeconds 쿠키를 유지할 초 단위 만료 시간
     * @return 공통 보안 속성이 적용된 응답 쿠키 객체
     */
    private ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    /**
     * 브라우저의 기존 토큰 쿠키를 제거하기 위한 만료 쿠키를 생성한다.
     * 같은 이름과 경로로 Max-Age 0 쿠키를 내려 기존 쿠키를 즉시 제거한다.
     *
     * @Author Seunghyeon.Kang
     * @param name 만료 처리할 쿠키 이름
     * @return 브라우저 쿠키 제거를 지시하는 만료 응답 쿠키 객체
     */
    private ResponseCookie createExpiredCookie(String name) {
        return createTokenCookie(name, "", 0);
    }

    /**
     * 요청 쿠키 목록에서 refreshToken 값을 추출한다.
     * 재발급과 logout 기능에서 Redis refreshToken 저장값을 찾기 위해 사용한다.
     *
     * @Author Seunghyeon.Kang
     * @param request refreshToken 쿠키가 포함될 수 있는 요청 객체
     * @return 요청 쿠키에서 찾은 refreshToken 문자열, 없으면 null
     */
    private String extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * 요청 쿠키 목록에서 accessToken 값을 추출한다.
     * 인증 상태 확인과 logout blacklist 등록 판단에 사용한다.
     *
     * @Author Seunghyeon.Kang
     * @param request accessToken 쿠키가 포함될 수 있는 요청 객체
     * @return 요청 쿠키에서 찾은 accessToken 문자열, 없으면 null
     */
    private String extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    /**
     * 요청 쿠키 배열에서 지정한 이름의 쿠키 값을 찾아 반환한다.
     * 쿠키 배열이 없거나 대상 쿠키가 없으면 호출부가 미인증 상태로 처리할 수 있도록 null을 반환한다.
     *
     * @Author Seunghyeon.Kang
     * @param request 쿠키 배열을 제공하는 HTTP 요청 객체
     * @param name 찾고자 하는 쿠키 이름
     * @return 지정한 이름과 일치하는 쿠키 값, 없으면 null
     */
    private String extractCookieValue(HttpServletRequest request, String name) {
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
     * 로그인 이력에 저장할 클라이언트 IP를 계산한다.
     * 프록시나 로드밸런서를 거친 요청은 실제 사용자 IP가 X-Forwarded-For 또는 X-Real-IP 헤더에 들어올 수 있다.
     * 관련 헤더가 없으면 서블릿 컨테이너가 제공하는 원격 주소를 사용한다.
     *
     * @Author Seunghyeon.Kang
     * @param request 로그인 콜백 요청 객체
     * @return 로그인 이력에 저장할 IP 주소 문자열
     */
    private String getLoginIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (!StringUtil.isEmpty(forwardedFor)) {
            // X-Forwarded-For에는 여러 IP가 쉼표로 들어올 수 있으므로 최초 진입 IP만 저장한다.
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (!StringUtil.isEmpty(realIp)) {
            // 단일 프록시 구성에서는 X-Real-IP에 실제 클라이언트 IP가 담기는 경우가 많다.
            return realIp;
        }

        // 별도 프록시 헤더가 없으면 WAS가 인식한 원격 주소를 로그인 IP로 저장한다.
        return request.getRemoteAddr();
    }

    /**
     * 로그인 이력에 저장할 User-Agent 값을 추출한다.
     * User-Agent는 사용자의 브라우저, 앱, OS 정보를 대략적으로 확인하는 사후 점검 정보로만 사용한다.
     *
     * @Author Seunghyeon.Kang
     * @param request 로그인 콜백 요청 객체
     * @return 로그인 이력에 저장할 User-Agent 문자열, 헤더가 없으면 null
     */
    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}
