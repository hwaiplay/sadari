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
 * fileName       : KakaoAuthProvider
 * author         : SeungHyeon.Kang
 * date           : 2026-03-15
 * description    : 사용자 외부 연동 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        SeungHyeon.Kang    최초 생성
 */
@Component
@Slf4j
public class KakaoAuthProvider {

    // BACK DOMAIN 설정값
    @Value("${domain.back}")
    private String BACK_DOMAIN;

    // KAKAO REDIRECT URI 설정값
    @Value("${kakao.redirect.uri}")
    private String KAKAO_REDIRECT_URI;

    // KAKAO CLIENT ID 설정값
    @Value("${kakao.key.restApi}")
    private String KAKAO_CLIENT_ID;

    /**
     * yml의 백엔드 도메인, 콜백 경로와 카카오 REST API 키로 로그인 인가 URL을 생성한다.
     *
     * @author SeungHyeon.Kang
     * @return 카카오 로그인 동의 화면 URL
     */
    public String getKakaoAuthorizationUrl() {

        // yml의 백엔드 도메인, 콜백 경로와 카카오 REST API 키로 로그인 인가 URL을 생성 결과를 반환한다
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
     * Kakao 인가 코드의 Access Token 교환한다.
     *
     * @author SeungHyeon.Kang
     * @param code Kakao 로그인 인가 코드
     * @return 처리 결과
     */
    public KakaoTokenDto getKakaoToken(String code) throws JsonProcessingException {

        // 외부 HTTP API 요청을 수행할 클라이언트를 담을 객체를 생성한다
        RestTemplate restTemplate = new RestTemplate();

        // 아래 처리 단계의 업무 목적을 설명한다.
        HttpHeaders headers = new HttpHeaders();
        // 처리한 값을 결과 컬렉션에 추가한다
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // 처리한 값을 결과 컬렉션에 추가한다
        params.add(AuthConstant.KAKAO_GRANT_TYPE, AuthConstant.KAKAO_AUTHORIZATION_CODE);
        // 처리한 값을 결과 컬렉션에 추가한다
        params.add(AuthConstant.KAKAO_CLIENT_ID, KAKAO_CLIENT_ID);
        // 처리한 값을 결과 컬렉션에 추가한다
        params.add(AuthConstant.KAKAO_REDIRECT_URI, getKakaoRedirectUri());
        // 처리한 값을 결과 컬렉션에 추가한다
        params.add(AuthConstant.KAKAO_CODE, code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        // 카카오 인증 서버에 토큰 또는 사용자 정보 요청을 전송한다
        ResponseEntity<String> response = restTemplate.exchange(
                AuthConstant.KAKAO_AUTHORIZATION_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        // 외부 API JSON 응답을 변환할 매퍼를 담을 객체를 생성한다
        ObjectMapper objectMapper = new ObjectMapper();
        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 외부 API의 JSON 응답을 업무 DTO로 변환한다
            KakaoTokenDto kakaoTokenDto = objectMapper.readValue(response.getBody(), KakaoTokenDto.class);
            // 진단에 필요한 처리 상태를 디버그 로그로 남긴다
            log.debug("Kakao 사용자 정보 응답 파싱에 성공했습니다.");
            // Kakao 인가 코드의 Access Token 교환 결과를 반환한다
            return kakaoTokenDto;
        }
        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (JsonProcessingException e) {

            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("Kakao 사용자 정보 응답 파싱에 실패했습니다.", e);
            throw e;
        }
    }

    /**
     * Kakao Access Token 기준 사용자 계정 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param vo 추가 또는 조회할 Kakao 사용자 정보
     * @return 처리 결과
     */
    public KakaoAccountDto getKakaoAccount(KakaoTokenDto vo) throws JsonProcessingException {

        // 외부 HTTP API 요청을 수행할 클라이언트를 담을 객체를 생성한다
        RestTemplate restTemplate = new RestTemplate();

        // 아래 처리 단계의 업무 목적을 설명한다.
        HttpHeaders headers = new HttpHeaders();
        // 처리한 값을 결과 컬렉션에 추가한다
        headers.add("Authorization", "Bearer " + vo.getAccess_token());
        // 처리한 값을 결과 컬렉션에 추가한다
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<?> request = new HttpEntity<>(headers);
        // 카카오 인증 서버에 토큰 또는 사용자 정보 요청을 전송한다
        ResponseEntity<String> accountInfoResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                request,
                String.class
        );

        // 외부 API JSON 응답을 변환할 매퍼를 담을 객체를 생성한다
        ObjectMapper objectMapper = new ObjectMapper();
        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 외부 API의 JSON 응답을 업무 DTO로 변환한다
            KakaoAccountDto kakaoAccountDto = objectMapper.readValue(accountInfoResponse.getBody(), KakaoAccountDto.class);
            // 진단에 필요한 처리 상태를 디버그 로그로 남긴다
            log.debug("Kakao 사용자 정보 응답 파싱에 성공했습니다.");
            // Kakao Access Token 기준 사용자 계정 조회 결과를 반환한다
            return kakaoAccountDto;
        }
        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (JsonProcessingException e) {

            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("Kakao 사용자 정보 응답 파싱에 실패했습니다.", e);
            throw e;
        }
    }

    /**
     * yml의 백엔드 도메인과 콜백 경로 사이의 슬래시를 하나로 정규화한다.
     *
     * @author SeungHyeon.Kang
     * @return 카카오 콘솔에 등록할 전체 OAuth 콜백 URI
     */
    private String getKakaoRedirectUri() {

        // 정규식과 일치하는 문자열을 일괄 치환한다
        String normalizedDomain = BACK_DOMAIN.replaceAll("/+$", "");
        // 정규식과 일치하는 문자열을 일괄 치환한다
        String normalizedPath = KAKAO_REDIRECT_URI.replaceAll("^/+", "");
        // yml의 백엔드 도메인과 콜백 경로 사이의 슬래시를 하나로 정규화 결과를 반환한다
        return normalizedDomain + "/" + normalizedPath;
    }
}
