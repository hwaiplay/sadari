package org.our.sadari.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.our.sadari.auth.domain.User;
import org.our.sadari.auth.repository.UserRepository;
import org.our.sadari.auth.security.JwtProvider;
import org.our.sadari.auth.vo.KakaoAccountVO;
import org.our.sadari.auth.vo.KakaoTokenVO;
import org.our.sadari.constant.AuthConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    // 토큰 요청
    public KakaoAccountVO getKakaoToken(String code) throws JsonProcessingException {
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

        log.debug("카카오 엑세스 토큰");

        return getKakaoAccount(kakaoTokenVO.getAccess_token());

    }

    // 사용자 정보 요청
    public KakaoAccountVO getKakaoAccount(String accessToken) throws JsonProcessingException {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<String> accountInfoResponse  = rt.exchange(
              "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            request,
            String.class
        );

        // JSON Parsing (-> kakaoAccountDto)
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoAccountVO kakaoAccountVO = null;
        try {
            kakaoAccountVO = objectMapper.readValue(accountInfoResponse.getBody(), KakaoAccountVO.class);
        } catch (JsonProcessingException e) { e.printStackTrace(); }

        log.debug("카카오 raw 응답: {}", accountInfoResponse.getBody());

        return kakaoAccountVO;
    }

    // 로그인
    public String kakaoLogin(String code) throws JsonProcessingException {

        // 1. 토큰 + 사용자 정보 가져오기
        KakaoAccountVO kakaoUser = getKakaoToken(code);

        String email = kakaoUser.kakao_account.email;
        String nickName = kakaoUser.kakao_account.profile.nickname;
        String providerId = String.valueOf(kakaoUser.id);

        // 2. DB 조회
        User user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> {
                    // 3. 회원가입
                    User newUser = User.builder()
                            // .email(email)
                            .nickname(nickName)
                            .provider("KAKAO")
                            .providerId(providerId)
                            .build();
                    return userRepository.save(newUser);
                });

        // 4. JWT 발급
        String token = jwtProvider.createToken(user.getUserNumb(), user.getNickname());

        log.debug("JWT 발급 완료: {}", token);

        return token;
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
