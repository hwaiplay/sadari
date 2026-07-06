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
import org.our.sadari.sadariUser.auth.service.AuthService;
import org.our.sadari.sadariUser.user.dto.TokenHistoryDto;
import org.our.sadari.sadariUser.user.mapper.TokenHistoryMapper;
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
 * author         : seungHyeon.Kang
 * date           : 2026-03-15
 * description    : 카카오 소셜 로그인
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        seungHyeon.Kang   최초 생성
 * 2026-03-17        hanWon.jang       리팩터리 및 JWT 토큰 발급
 * 2026-03-24        hanWon.jang       ResultData로 응답 통일, 로그인 상태 확인 API 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthLoginController {

    // accessToken은 한 시간 동안 유지한다.
    private static final long ACCESS_TOKEN_COOKIE_MAX_AGE_SECONDS = 60 * 60;
    // refreshToken은 일주일 동안 유지한다.
    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 7;

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final TokenHistoryMapper tokenMapper;

    @Value("${domain.front}")
    private String frontDomain;

    @GetMapping("/tokenCheck")
    public ResultData tokenCheck(HttpServletRequest request) {
        // 로그인 상태 확인은 accessToken 쿠키를 기준으로 판단한다.
        String accessToken = extractAccessToken(request);

        // accessToken 쿠키가 없으면 로그인하지 않은 상태로 판단한다.
        if (StringUtil.isEmpty(accessToken)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // accessToken이 변조되었거나 만료되었으면 유효하지 않은 토큰으로 판단한다.
        if (!jwtProvider.validateToken(accessToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // accessToken이 정상이라면 로그인 상태로 응답한다.
        return ResultData.success();
    }

    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws Exception {
        // 카카오 인증 코드로 사용자 정보를 확인하고 토큰을 발급한다.
        TokenDto token = authService.kakaoLogin(code);

        // refreshToken은 브라우저 쿠키와 DB에 함께 보관된다.
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createRefreshTokenCookie(token.getRefreshToken()).toString()
        );
        // accessToken은 API 인증에 사용되며 한 시간 동안 유지된다.
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createAccessTokenCookie(token.getAccessToken()).toString()
        );

        // 로그인 처리가 끝나면 프론트 인증 완료 화면으로 이동한다.
        response.sendRedirect(frontDomain + "/oauth");
    }

    @PostMapping("/refresh")
    public ResultData refresh(HttpServletRequest request, HttpServletResponse response) {
        // accessToken 재발급은 refreshToken 쿠키를 기준으로 처리한다.
        String refreshToken = extractRefreshToken(request);

        // refreshToken 쿠키가 없으면 재발급할 수 없는 상태다.
        if (StringUtil.isEmpty(refreshToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // DB에 저장된 refreshToken과 요청 쿠키의 refreshToken을 대조한다.
        TokenHistoryDto tokenDto = tokenMapper.getRefreshToken(refreshToken);

        // DB에 refreshToken이 없으면 이미 로그아웃되었거나 유효하지 않은 토큰이다.
        if (StringUtil.isEmpty(tokenDto)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // DB 기준 만료 시간이 지났으면 재발급할 수 없다.
        if (tokenDto.isExpired()) {
            return ResultData.fail(ResultEnum.TOKEN_EXPIRED);
        }

        // JWT 자체가 변조되었거나 만료되었으면 재발급할 수 없다.
        if (!jwtProvider.validateToken(refreshToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // refreshToken에서 사용자 번호를 꺼내 새 accessToken을 발급한다.
        Long userNumb = jwtProvider.getUserNumb(refreshToken);
        String newAccessToken = jwtProvider.createAccessToken(userNumb, AuthConstant.ROLE_USER);

        // 새 accessToken을 쿠키로 내려주어 다음 요청부터 인증에 사용하게 한다.
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createAccessTokenCookie(newAccessToken).toString()
        );

        // 재발급이 끝나면 성공 응답을 반환한다.
        return ResultData.success();
    }

    private ResponseCookie createAccessTokenCookie(String accessToken) {
        // accessToken 쿠키는 자바스크립트에서 읽지 못하게 설정한다.
        return ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .path("/")
                .maxAge(ACCESS_TOKEN_COOKIE_MAX_AGE_SECONDS)
                .build();
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        // refreshToken 쿠키는 accessToken 재발급 요청에만 사용한다.
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .path("/")
                .maxAge(REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS)
                .build();
    }

    private String extractRefreshToken(HttpServletRequest request) {
        // refreshToken 쿠키 값을 추출한다.
        return extractCookieValue(request, "refreshToken");
    }

    private String extractAccessToken(HttpServletRequest request) {
        // accessToken 쿠키 값을 추출한다.
        return extractCookieValue(request, "accessToken");
    }

    private String extractCookieValue(HttpServletRequest request, String name) {
        // 요청에 쿠키가 없으면 찾을 토큰도 없다.
        if (StringUtil.isEmpty(request.getCookies())) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            // 요청한 이름과 일치하는 쿠키를 찾으면 값을 반환한다.
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        // 원하는 이름의 쿠키가 없으면 null을 반환한다.
        return null;
    }
}
