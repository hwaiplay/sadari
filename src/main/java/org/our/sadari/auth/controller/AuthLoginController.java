package org.our.sadari.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.auth.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
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
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthLoginController {

    private final AuthService authService;

    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin (@RequestParam("code") String code, HttpServletResponse response) throws Exception {

        String token = authService.kakaoLogin(code);

        response.sendRedirect(
            "http://localhost:5173/oauth?token=" + token
        );
    }
}