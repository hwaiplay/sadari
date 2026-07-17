package org.our.sadari.user.auth.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.user.auth.dto.KakaoAccountDto;
import org.our.sadari.user.auth.dto.KakaoTokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * KakaoAuthProvider 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Component
@Slf4j
public class KakaoAuthProvider {

    @Value("${domain.back}")
    private String BACK_DOMAIN;

    @Value("${kakao.redirect.uri}")
    private String KAKAO_REDIRECT_URI;

    @Value("${kakao.key.restApi}")
    private String KAKAO_CLIENT_ID;

    /**
     * getKakaoToken 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param code 처리에 필요한 입력값
     * @return 처리 결과
     */
    public KakaoTokenDto getKakaoToken(String code) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        // 아래 처리 단계의 업무 목적을 설명한다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(AuthConstant.KAKAO_GRANT_TYPE, AuthConstant.KAKAO_AUTHORIZATION_CODE);
        params.add(AuthConstant.KAKAO_CLIENT_ID, KAKAO_CLIENT_ID);
        params.add(AuthConstant.KAKAO_REDIRECT_URI, BACK_DOMAIN + KAKAO_REDIRECT_URI);
        params.add(AuthConstant.KAKAO_CODE, code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                AuthConstant.KAKAO_AUTHORIZATION_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            KakaoTokenDto kakaoTokenDto = objectMapper.readValue(response.getBody(), KakaoTokenDto.class);
            log.debug("Kakao 사용자 정보 응답 파싱에 성공했습니다.");
            return kakaoTokenDto;
        } catch (JsonProcessingException e) {
            log.error("Kakao 사용자 정보 응답 파싱에 실패했습니다.", e);
            throw e;
        }
    }

    /**
     * getKakaoAccount 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param vo 처리에 필요한 입력값
     * @return 처리 결과
     */
    public KakaoAccountDto getKakaoAccount(KakaoTokenDto vo) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        // 아래 처리 단계의 업무 목적을 설명한다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + vo.getAccess_token());
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<String> accountInfoResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                request,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            KakaoAccountDto kakaoAccountDto = objectMapper.readValue(accountInfoResponse.getBody(), KakaoAccountDto.class);
            log.debug("Kakao 사용자 정보 응답 파싱에 성공했습니다.");
            return kakaoAccountDto;
        } catch (JsonProcessingException e) {
            log.error("Kakao 사용자 정보 응답 파싱에 실패했습니다.", e);
            throw e;
        }
    }
}
