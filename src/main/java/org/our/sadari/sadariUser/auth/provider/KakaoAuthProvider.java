package org.our.sadari.sadariUser.auth.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.sadariUser.auth.dto.KakaoAccountDto;
import org.our.sadari.sadariUser.auth.dto.KakaoTokenDto;
import org.our.sadari.global.common.constant.AuthConstant;
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
 * fileName       : KakaoAuthProvider
 * author         : SeungHyeon.Kang
 * date           : 2026-03-21
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21        SeungHyeon.Kang       최초 생성
 */
@Component
@Slf4j
public class KakaoAuthProvider {

    @Value("${domain.back}")
    private String BACK_DOMAIN;  //call back uri

    @Value("${kakao.redirect.uri}")
    private String KAKAO_REDIRECT_URI;  //call back uri

    @Value("${kakao.key.restApi}")
    private String KAKAO_CLIENT_ID; //카카오 앱 REST API 키

    /**
     * 로그인 로직 (회원가입 여부 확인 및 JWT 발급)
     * @param: 발급된 카카오 액세스 토큰
     * @return: 컨트롤러에서 전달받은 인가 코드
     */
    public KakaoTokenDto getKakaoToken(String code) throws JsonProcessingException {
        // HTTP 요청을 보내기 위한 RestTemplate 객체 생성
        RestTemplate rt = new RestTemplate();

        // HTTP Header 설정: 카카오 토큰 요청은 x-www-form-urlencoded 형식을 사용해야 함
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 설정: 토큰 발급에 필요한 필수 파라미터들을 담음
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(AuthConstant.KAKAO_GRANT_TYPE, AuthConstant.KAKAO_AUTHORIZATION_CODE); // 고정값: authorization_code
        params.add(AuthConstant.KAKAO_CLIENT_ID, KAKAO_CLIENT_ID);                        // REST API 키
        params.add(AuthConstant.KAKAO_REDIRECT_URI, BACK_DOMAIN + KAKAO_REDIRECT_URI);    // 등록된 리다이렉트 URI
        params.add(AuthConstant.KAKAO_CODE, code);                                        // 발급받은 인가 코드

        // Header와 Body를 하나의 Entity로 합침
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 카카오 토큰 서버로 POST 요청 전송 및 응답 수신
        ResponseEntity<String> response = rt.exchange(
                AuthConstant.KAKAO_AUTHORIZATION_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        // JSON 응답 문자열을 KakaoTokenVO 객체로 역직렬화(Parsing)
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoTokenDto kakaoTokenDto = new KakaoTokenDto();
        try {
            kakaoTokenDto = objectMapper.readValue(response.getBody(), KakaoTokenDto.class);
        } catch (JsonProcessingException e) {
            log.error("카카오 엑세스 토큰 발급 중 에러발생 {}", e.getMessage());
            log.debug("카카오 엑세스 토큰 발급 중 에러발생: ", e.getStackTrace());
        }

        log.debug("카카오 엑세스 토큰 발급 완료");

        // 발급받은 액세스 토큰을 사용하여 사용자 정보를 가져오는 메서드 호출
        return kakaoTokenDto;
    }

    /**
     * 인가 코드로 카카오 액세스 토큰 요청
     * @param: 발급된 카카오 액세스 토큰
     * @return: KakaoAccountVO
     */
    public KakaoAccountDto getKakaoAccount(KakaoTokenDto vo) throws JsonProcessingException {

        String accessToken = vo.getAccess_token();
        RestTemplate rt = new RestTemplate();

        // HTTP Header 설정: 'Bearer ' 방식의 인증 토큰 전송
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // Body 없이 Header만 담아 요청 객체 생성
        HttpEntity<?> request = new HttpEntity<>(headers);

        // 카카오 API 서버에 사용자 정보 요청 (v2/user/me)
        ResponseEntity<String> accountInfoResponse = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                request,
                String.class
        );

        // 응답받은 JSON 데이터를 KakaoAccountVO 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoAccountDto kakaoAccountDto = null;
        try {
            kakaoAccountDto = objectMapper.readValue(accountInfoResponse.getBody(), KakaoAccountDto.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 에러 발생: {}", e.getMessage());
            log.debug("JSON 파싱 에러 발생: ", e.getStackTrace());
        }

        log.debug("카카오 사용자 정보 조회 완료");
        return kakaoAccountDto;
    }
}
