package org.our.sadari.user.auth.controller;

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
import org.our.sadari.user.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
     * tokenCheck 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @return 처리 결과
     */
    @GetMapping("/tokenCheck")
    public ResultData tokenCheck(HttpServletRequest request) {
        String accessToken = extractAccessToken(request);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(accessToken)) {
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (!jwtProvider.validateToken(accessToken)) {
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(accessToken))) {
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        return ResultData.success();
    }

    /**
     * kakaoAuthLogin 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     */
    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin(@RequestParam("code") String code
                                , HttpServletRequest request
                                , HttpServletResponse response) throws Exception {

        ResultData loginResult = authService.kakaoLogin(code, getLoginIp(request), getUserAgent(request));

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (loginResult.getCode() != 200) {
            expireTokenCookies(response);
            response.sendRedirect(frontDomain + "/oauth");
            return;
        }

        TokenDto token = (TokenDto) loginResult.getData();

        // 아래 처리 단계의 업무 목적을 설명한다.
        addTokenCookies(response, token.getAccessToken(), token.getRefreshToken());
        response.sendRedirect(frontDomain + "/oauth");
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
    public ResultData refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(refreshToken) || !jwtProvider.validateToken(refreshToken)) {
            expireTokenCookies(response);
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        Long userNumb = jwtProvider.getUserNumb(refreshToken);
        String savedRefreshToken = tokenRedisService.getRefreshToken(userNumb);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(savedRefreshToken) || !savedRefreshToken.equals(refreshToken)) {
            expireTokenCookies(response);
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        String newAccessToken = jwtProvider.createAccessToken(userNumb, AuthConstant.ROLE_USER);
        String newRefreshToken = jwtProvider.createRefreshToken(userNumb);

        // 아래 처리 단계의 업무 목적을 설명한다.
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
    public ResultData logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = extractAccessToken(request);
        String refreshToken = extractRefreshToken(request);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (!StringUtil.isEmpty(accessToken) && jwtProvider.validateToken(accessToken)) {
            tokenRedisService.setAccessTokenBlacklist(
                    jwtProvider.getTokenId(accessToken),
                    jwtProvider.getRemainingSeconds(accessToken)
            );
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (!StringUtil.isEmpty(refreshToken) && jwtProvider.validateToken(refreshToken)) {
            tokenRedisService.deleteRefreshToken(jwtProvider.getUserNumb(refreshToken));
        }

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
     * expireTokenCookies 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param response 처리에 필요한 입력값
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
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(request.getCookies())) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * getLoginIp 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @return 처리 결과
     */
    private String getLoginIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (!StringUtil.isEmpty(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (!StringUtil.isEmpty(realIp)) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * getUserAgent 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @return 처리 결과
     */
    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}
