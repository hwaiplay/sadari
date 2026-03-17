package org.our.sadari.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.our.sadari.auth.service.AuthService;
import org.our.sadari.auth.service.AuthServiceImpl;
import org.our.sadari.auth.vo.KakaoTokenVO;
import org.our.sadari.constant.AuthConstant;
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

import com.fasterxml.jackson.core.JsonProcessingException;

import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @GetMapping("/callback/kakao")
    public KakaoTokenVO kakaoAuthLogin (@RequestParam("code") String code) throws JsonProcessingException {

        // log.debug("Try kakao Login :: " + code);

        // RestTemplate rt = new RestTemplate(); //통신용
        // HttpHeaders headers = new HttpHeaders();
        // headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // // HttpBody 객체 생성
        // MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // params.add(AuthConstant.GRANT_TYPE, "authorization_code"); //카카오 공식문서 기준 authorization_code 로 고정
        // params.add(AuthConstant.CLIENT_ID, KAKAO_CLIENT_ID);
        // params.add(AuthConstant.REDIRECT_URI, BACK_DOMAIN + KAKAO_REDIRECT_URI);
        // params.add(AuthConstant.CODE, code); //인가 코드 요청시 받은 인가 코드값, 프론트에서 받아오는 그 코드

        // // 헤더와 바디 합치기 위해 HttpEntity 객체 생성
        // HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);
        // System.out.println(kakaoTokenRequest);

        // // 카카오로부터 Access token 수신
        // ResponseEntity<String> accessTokenResponse = rt.exchange(
        //         AuthConstant.AUTHORIZATION_URL,
        //         HttpMethod.POST,
        //         kakaoTokenRequest,
        //         String.class
        // );

        // ObjectMapper objectMapper = new ObjectMapper();
        // KakaoTokenVO kakaoTokenVO = null;
        // kakaoTokenVO = objectMapper.readValue(accessTokenResponse.getBody(), KakaoTokenVO.class);

        // log.debug("get AccessToken" + accessTokenResponse);
        // System.out.println("get AccessToken" + accessTokenResponse);

        // return kakaoTokenVO;

        return authServiceImpl.getKakaoToken(code);
    }
}