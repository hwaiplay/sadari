package org.our.sadari.sadariUser.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.exception.ResultEnum;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.sadariUser.auth.entity.TokenHistoryEntity;
import org.our.sadari.sadariUser.auth.repository.TokenHistoryRepository;
import org.our.sadari.sadariUser.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthLoginController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final TokenHistoryRepository tokenHistoryRepository;

    // 로그인 상태 확인 API
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {

        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        TokenHistoryEntity tokenEntity = tokenHistoryRepository
                .findByRefrTokn(refreshToken)
                .orElse(null);

        if (tokenEntity == null || tokenEntity.isExpired()) {
            return ResponseEntity.status(401).build();
        }

        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin (@RequestParam("code") String code,
                                HttpServletResponse response) throws Exception {

        TokenDto token = authService.kakaoLogin(code);

        //refreshToken은 쿠키로 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token.getRefreshToken())
                .httpOnly(true)
                // .secure(true) // HTTPS 환경에서만 쿠키가 전송되도록 설정 (개발 환경에서는 주석 처리)
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        //accessToken만 프론트로 전달
        response.sendRedirect(
                "http://localhost:5173/oauth?accessToken=" + token.getAccessToken()
        );
    }

    /**
     * 토큰 재발급 API
     * @param request
     * @return
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refresh(HttpServletRequest request) {

        // 쿠키에서 refreshToken 꺼내기
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            // 인증 자체가 안 된 상태
            throw new CustomException(ResultEnum.AUTH_FAIL, HttpStatus.UNAUTHORIZED);
        }

        // 조회 (존재 여부 확인)
        TokenHistoryEntity tokenEntity = tokenHistoryRepository
                .findByRefrTokn(refreshToken)
                .orElseThrow(() ->
                        new CustomException(ResultEnum.TOKEN_INVALID, HttpStatus.UNAUTHORIZED)
                );

        // 만료 체크 (DB 기준)
        if (tokenEntity.isExpired()) {
            throw new CustomException(ResultEnum.TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED);
        }

        // JWT 자체 검증 (위조 체크)
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(ResultEnum.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        }

        // userNumb 추출
        Long userNumb = jwtProvider.getUserNumb(refreshToken);

        // 새 accessToken 발급
        String newAccessToken = jwtProvider.createAccessToken(userNumb, AuthConstant.ROLE_USER);

        // 응답
        return ResponseEntity.ok(new TokenDto(newAccessToken, refreshToken));
    }

    /**
     * 쿠키에서 refreshToken 추출
     * @return refreshToken 값 (없으면 null)
     */
    private String extractRefreshToken(HttpServletRequest request) {

        // 쿠키 자체가 없는 경우
        if (request.getCookies() == null) {
            return null;
        }

        // refreshToken 쿠키 찾기
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}