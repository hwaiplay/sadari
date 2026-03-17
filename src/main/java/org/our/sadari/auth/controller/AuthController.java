package org.our.sadari.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.auth.service.AuthServiceImpl;
import org.our.sadari.auth.vo.KakaoAccountVO;
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
    public KakaoAccountVO kakaoAuthLogin (@RequestParam("code") String code) throws JsonProcessingException {
        return authServiceImpl.getKakaoToken(code);
    }
}