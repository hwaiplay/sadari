package org.our.sadari.auth.service;

import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl {

   /* private final KakaoAuthProvider kakaoAuthProvider;

    @Override
    public AuthResponseVO.LoginResponse kakaoLogin(String code) {

        // 1. 인가 코드로 토큰 발급
        String accessToken = kakaoAuthProvider.getAccessToken(code);

        // 2. 토큰으로 카카오 유저 정보 가져오기
        HashMap<String, Object> userInfo = kakaoAuthProvider.getKakaoUserInfo(accessToken);

        // 3. 카카오 유저 정보로 로그인
        AuthResponseVO.LoginResponse kakaoUserResponse = kakaoAuthProvider.kakaoUserLogin(userInfo);

        return kakaoUserResponse;
    }*/
}
