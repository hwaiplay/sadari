package org.our.sadari.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/oauth")
public class AuthController {

    @GetMapping("/callback/kakao")
    public String kakaoAuthLogin (@RequestParam("code") String code) {

        System.out.println("kakao auth login ::" + code);

        return "kakao";
    }

}