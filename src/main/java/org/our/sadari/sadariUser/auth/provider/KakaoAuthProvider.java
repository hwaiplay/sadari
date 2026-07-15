package org.our.sadari.sadariUser.auth.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.sadariUser.auth.dto.KakaoAccountDto;
import org.our.sadari.sadariUser.auth.dto.KakaoTokenDto;
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
 * Kakao OAuth API 연동 제공자.
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
     * 인가 코드를 Kakao access token으로 교환한다.
     *
     * @author Seunghyeon.Kang
     * @param code Kakao 로그인 후 발급받은 인가 코드
     * @return Kakao token 응답 DTO
     * @throws JsonProcessingException Kakao token 응답 JSON 변환 중 오류가 발생한 경우
     */
    public KakaoTokenDto getKakaoToken(String code) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        // Kakao token API는 form-urlencoded 방식으로 파라미터를 전달해야 한다.
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
            log.debug("Kakao access token 발급 완료");
            return kakaoTokenDto;
        } catch (JsonProcessingException e) {
            log.error("Kakao access token 발급 응답 파싱 중 오류가 발생했습니다.", e);
            throw e;
        }
    }

    /**
     * Kakao access token으로 사용자 계정 정보를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param vo Kakao token 응답 DTO
     * @return Kakao 계정 정보 DTO
     * @throws JsonProcessingException Kakao 사용자 응답 JSON 변환 중 오류가 발생한 경우
     */
    public KakaoAccountDto getKakaoAccount(KakaoTokenDto vo) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        // Kakao 사용자 정보 API는 Bearer token을 Authorization 헤더로 전달한다.
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
            log.debug("Kakao 사용자 정보 조회 완료");
            return kakaoAccountDto;
        } catch (JsonProcessingException e) {
            log.error("Kakao 사용자 정보 응답 파싱 중 오류가 발생했습니다.", e);
            throw e;
        }
    }
}
