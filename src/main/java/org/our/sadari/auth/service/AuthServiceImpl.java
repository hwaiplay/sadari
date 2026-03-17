package org.our.sadari.auth.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.our.sadari.auth.vo.KakaoAccountVO;
import org.our.sadari.auth.vo.KakaoTokenVO;
import org.our.sadari.constant.AuthConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl {

    @Value("${domain.back}")
    private String BACK_DOMAIN;  //call back uri

    @Value("${kakao.redirect.uri}")
    private String KAKAO_REDIRECT_URI;  //call back uri

    @Value("${kakao.key.restApi}")
    private String KAKAO_CLIENT_ID; //카카오 앱 REST API 키

    // 토큰 요청
    public KakaoTokenVO getKakaoToken(String code) throws JsonProcessingException {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(AuthConstant.GRANT_TYPE, "authorization_code");
        params.add(AuthConstant.CLIENT_ID, KAKAO_CLIENT_ID);
        params.add(AuthConstant.REDIRECT_URI, BACK_DOMAIN + KAKAO_REDIRECT_URI);
        params.add(AuthConstant.CODE, code);

        HttpEntity<MultiValueMap<String, String>> request =
            new HttpEntity<>(params, headers);

        ResponseEntity<String> response = rt.exchange(
            AuthConstant.AUTHORIZATION_URL,
            HttpMethod.POST,
            request,
            String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoTokenVO kakaoTokenVO = objectMapper.readValue(response.getBody(), KakaoTokenVO.class);

        // log.debug("카카오 엑세스 토큰: {}", response.getBody());

        return kakaoTokenVO;

    }

    // 사용자 정보 요청
    public String getKakaoAccount(String accessToken) throws JsonProcessingException {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = rt.exchange(
              "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            request,
            String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        // KakaoAccountVO kakaoAccountVO = objectMapper.readValue(response.getBody(), KakaoAccountVO.class);

        log.debug("카카오 raw 응답: {}", response.getBody());

        return response.getBody();
    }

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
