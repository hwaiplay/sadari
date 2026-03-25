package org.our.sadari.sadariUser.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.sadariUser.auth.entity.TokenHistoryEntity;
import org.our.sadari.sadariUser.auth.repository.TokenHistoryRepository;
import org.our.sadari.sadariUser.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

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
 * 2026-03-25        hanWon.jang       리팩터링
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthLoginController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final TokenHistoryRepository tokenHistoryRepository;

    /**
     * 로그인 상태 확인 + 자동 재발급
     */
    @GetMapping("/tokenCheck")
    public ResultData<?> tokenCheck(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = extractAccessToken(request);

        // 1️⃣ accessToken이 유효하면 그대로 통과
        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            return ResultData.success();
        }

        // 2️⃣ refreshToken으로 재발급 시도
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        TokenHistoryEntity tokenEntity = tokenHistoryRepository
                .findByRefrTokn(refreshToken)
                .orElse(null);

        if (tokenEntity == null || tokenEntity.isExpired()) {
            return ResultData.fail(ResultEnum.TOKEN_EXPIRED);
        }

        if (!jwtProvider.validateToken(refreshToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 3️⃣ 새 accessToken 발급
        Long userNumb = jwtProvider.getUserNumb(refreshToken);
        String newAccessToken = jwtProvider.createAccessToken(userNumb, AuthConstant.ROLE_USER);

        // 4️⃣ 쿠키에 다시 저장
        addAccessTokenCookie(response, newAccessToken);

        log.info("accessToken 재발급 완료");

        return ResultData.success();
    }

    /**
     * 카카오 로그인 콜백
     */
    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin (@RequestParam("code") String code,
                                HttpServletResponse response) throws Exception {

        TokenDto token = authService.kakaoLogin(code);

        addRefreshTokenCookie(response, token.getRefreshToken());
        addAccessTokenCookie(response, token.getAccessToken());

        response.sendRedirect("http://localhost:5173/oauth");
    }

    /**
     * refreshToken 기반 accessToken 재발급 (직접 호출용)
     */
    @PostMapping("/refresh")
    public ResultData<?> refresh(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        TokenHistoryEntity tokenEntity = tokenHistoryRepository
                .findByRefrTokn(refreshToken)
                .orElseThrow(() ->
                        new CustomException(ResultEnum.TOKEN_INVALID, HttpStatus.UNAUTHORIZED)
                );

        if (tokenEntity.isExpired()) {
            return ResultData.fail(ResultEnum.TOKEN_EXPIRED);
        }

        if (!jwtProvider.validateToken(refreshToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        Long userNumb = jwtProvider.getUserNumb(refreshToken);
        String newAccessToken = jwtProvider.createAccessToken(userNumb, AuthConstant.ROLE_USER);

        addAccessTokenCookie(response, newAccessToken);

        return ResultData.success();
    }

    // =========================
    // 🔽 쿠키 처리 메서드 분리
    // =========================
    private void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .sameSite("Lax") // 배포 시 None + secure
                .path("/")
                .maxAge(60 * 60)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String extractAccessToken(HttpServletRequest request) {
        return extractCookie(request, "accessToken");
    }

    private String extractRefreshToken(HttpServletRequest request) {
        return extractCookie(request, "refreshToken");
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}