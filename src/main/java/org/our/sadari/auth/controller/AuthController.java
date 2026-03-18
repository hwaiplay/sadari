package org.our.sadari.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.auth.service.AuthServiceImpl;
import org.our.sadari.auth.vo.KakaoAccountVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin (@RequestParam("code") String code, HttpServletResponse response) throws Exception {
        // return authServiceImpl.kakaoLogin(code);
        String token = authServiceImpl.kakaoLogin(code);

        response.sendRedirect(
            "http://localhost:5173/oauth?token=" + token
        );
    }
}