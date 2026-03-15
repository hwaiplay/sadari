package org.our.sadari.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.auth.vo.KakaoTokenVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthController {

    @Value("${domain.back}")
    private String BACK_DOMAIN;  //call back uri

    @Value("${kakao.redirect.uri}")
    private String KAKAO_REDIRECT_URI;  //call back uri

    @Value("${kakao.key.restApi}")
    private String KAKAO_CLIENT_ID; //카카오 앱 REST API 키

    @GetMapping("/callback/kakao")
    public KakaoTokenVO kakaoAuthLogin (@RequestParam("code") String code) {

        log.debug("Try kakao Login :: " + code);

        RestTemplate rt = new RestTemplate(); //통신용
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpBody 객체 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code"); //카카오 공식문서 기준 authorization_code 로 고정
        params.add("client_id", KAKAO_CLIENT_ID);
        params.add("redirect_uri", BACK_DOMAIN + KAKAO_REDIRECT_URI);
        params.add("code", code); //인가 코드 요청시 받은 인가 코드값, 프론트에서 받아오는 그 코드

        // 헤더와 바디 합치기 위해 HttpEntity 객체 생성
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);
        System.out.println(kakaoTokenRequest);

        // 카카오로부터 Access token 수신
        ResponseEntity<String> accessTokenResponse = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoTokenVO kakaoTokenVO = null;
        kakaoTokenVO = objectMapper.readValue(accessTokenResponse.getBody(), KakaoTokenVO.class);

        log.debug("get AccessToken" + accessTokenResponse);
        System.out.println("get AccessToken" + accessTokenResponse);

        return kakaoTokenVO;
    }

}