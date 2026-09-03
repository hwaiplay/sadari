package org.our.sadari.user.auth.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.user.auth.dto.KakaoTokenDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * fileName       : KakaoAuthProviderTest
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : Kakao 인증 제공자의 공용 HTTP 의존성과 설정 URL 사용을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang         최초 생성
 */
@ExtendWith(MockitoExtension.class)
class KakaoAuthProviderTest {

    // Kakao 외부 요청을 관찰할 공용 HTTP 클라이언트
    @Mock
    private RestTemplate restTemplate;

    // 설정 URL과 공용 의존성을 검증할 인증 제공자
    private KakaoAuthProvider kakaoAuthProvider;

    /**
     * 테스트마다 공용 의존성과 Kakao 설정값을 주입함
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUp() {

        // 운영과 같은 생성자 주입 경로로 공용 HTTP 클라이언트와 JSON 매퍼를 전달함
        kakaoAuthProvider = new KakaoAuthProvider(restTemplate, new ObjectMapper());
        // 외부 환경에 의존하지 않도록 테스트 전용 설정값을 주입함
        ReflectionTestUtils.setField(kakaoAuthProvider, "backDomain", "https://api.example.com/");
        ReflectionTestUtils.setField(kakaoAuthProvider, "kakaoRedirectUri", "/api/oauth/callback/kakao");
        ReflectionTestUtils.setField(kakaoAuthProvider, "kakaoClientId", "test-client");
        ReflectionTestUtils.setField(kakaoAuthProvider, "kakaoAuthorizeUrl", "https://auth.example.com/authorize");
        ReflectionTestUtils.setField(kakaoAuthProvider, "kakaoTokenUrl", "https://auth.example.com/token");
        ReflectionTestUtils.setField(kakaoAuthProvider, "kakaoUserInfoUrl", "https://api.example.com/user");
        ReflectionTestUtils.setField(kakaoAuthProvider, "kakaoUnlinkUrl", "https://api.example.com/unlink");
    }

    /**
     * 로그인 인가 URL이 코드 상수가 아니라 주입된 Endpoint와 정규화된 콜백을 사용하는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void getLoginUrlUsesConfig() {

        // 테스트 설정으로 Kakao 로그인 인가 URL을 생성함
        String loginUrl = kakaoAuthProvider.getKakaoLoginUrl("state-value");

        // Kakao 인가 Endpoint 설정값이 URL의 기준 주소로 사용되는지 확인함
        assertTrue(loginUrl.startsWith("https://auth.example.com/authorize?"));
        // 도메인과 콜백 경로의 중복 슬래시가 제거되는지 확인함
        assertTrue(loginUrl.contains("redirect_uri=https://api.example.com/api/oauth/callback/kakao"));
    }

    /**
     * 토큰 교환이 주입된 HTTP 클라이언트와 JSON 매퍼를 재사용하는지 검증함
     *
     * @author HanWon.Jang
     * @throws Exception 모의 JSON 응답 변환에 실패한 경우
     */
    @Test
    void getTokenUsesSharedClient() throws Exception {

        // Kakao 토큰 Endpoint의 정상 JSON 응답을 모의함
        when(restTemplate.exchange(
                eq("https://auth.example.com/token")
              , eq(HttpMethod.POST)
              , any(HttpEntity.class)
              , eq(String.class)
        )).thenReturn(ResponseEntity.ok("{\"access_token\":\"shared-token\"}"));

        // 공용 의존성을 통해 인가 코드를 토큰으로 교환함
        KakaoTokenDto tokenDto = kakaoAuthProvider.getKakaoToken("authorization-code");

        // 공용 JSON 매퍼가 Kakao 응답의 Access Token을 변환했는지 확인함
        assertEquals("shared-token", tokenDto.getAccess_token());
        // 주입된 HTTP 클라이언트가 설정된 토큰 Endpoint로 요청했는지 확인함
        verify(restTemplate).exchange(
                eq("https://auth.example.com/token")
              , eq(HttpMethod.POST)
              , any(HttpEntity.class)
              , eq(String.class)
        );
    }
}
