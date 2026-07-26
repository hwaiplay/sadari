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
import org.springframework.web.util.UriComponentsBuilder;

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
     * yml의 백엔드 도메인, 콜백 경로와 카카오 REST API 키로 로그인 인가 URL을 생성한다.
     *
     * @author Seunghyeon.Kang
     * @return 카카오 로그인 동의 화면 URL
     */
    public String getKakaoAuthorizationUrl() {
        return UriComponentsBuilder
                .fromUriString(AuthConstant.KAKAO_AUTHORIZE_URL)
                .queryParam(AuthConstant.KAKAO_CLIENT_ID, KAKAO_CLIENT_ID)
                .queryParam(AuthConstant.KAKAO_REDIRECT_URI, getKakaoRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "profile_nickname,profile_image")
                .build()
                .encode()
                .toUriString();
    }

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
        params.add(AuthConstant.KAKAO_REDIRECT_URI, getKakaoRedirectUri());
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

    /**
     * yml의 백엔드 도메인과 콜백 경로 사이의 슬래시를 하나로 정규화한다.
     *
     * @author Seunghyeon.Kang
     * @return 카카오 콘솔에 등록할 전체 OAuth 콜백 URI
     */
    private String getKakaoRedirectUri() {
        String normalizedDomain = BACK_DOMAIN.replaceAll("/+$", "");
        String normalizedPath = KAKAO_REDIRECT_URI.replaceAll("^/+", "");
        return normalizedDomain + "/" + normalizedPath;
    }
}
