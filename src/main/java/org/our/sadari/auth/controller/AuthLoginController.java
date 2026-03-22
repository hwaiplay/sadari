package org.our.sadari.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.auth.entity.TokenHistoryEntity;
import org.our.sadari.auth.repository.TokenHistoryRepository;
import org.our.sadari.auth.service.AuthService;
import org.our.sadari.security.dto.TokenDto;
import org.our.sadari.security.jwt.JwtProvider;
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

    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin (@RequestParam("code") String code,
                                HttpServletResponse response) throws Exception {

        TokenDto token = authService.kakaoLogin(code);

        //refreshToken은 쿠키로 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        //accessToken만 프론트로 전달
        response.sendRedirect(
                "http://localhost:5173/oauth?accessToken=" + token.getAccessToken()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refresh(HttpServletRequest request) {

        // 1. 쿠키에서 refreshToken 꺼내기
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. DB 조회
        TokenHistoryEntity tokenEntity = tokenHistoryRepository
                .findByRefrTokn(refreshToken)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 refreshToken"));

        // 3. 만료 체크 (DB 기준)
        if (tokenEntity.isExpired()) {
            throw new RuntimeException("refreshToken 만료");
        }

        // 4. JWT 자체 검증 (위조 체크)
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new RuntimeException("토큰 위조");
        }

        // 5. userNumb 추출
        Long userNumb = jwtProvider.getUserNumb(refreshToken);

        // 6. 새 accessToken 발급
        String newAccessToken = jwtProvider.createAccessToken(userNumb, "USER");

        // 7. 응답 (refreshToken은 그대로 사용)
        return ResponseEntity.ok(new TokenDto(newAccessToken, refreshToken));
    }

    private String extractRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}