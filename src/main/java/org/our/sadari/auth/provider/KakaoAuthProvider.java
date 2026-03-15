package org.our.sadari.auth.provider;

import org.springframework.stereotype.Component;

@Component
public class KakaoAuthProvider {

    /*private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${kakao.key.restApi}")
    private String clientId;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    public String getAccessToken(String code) {

       /* HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = null;

        try {
            response =
                    restTemplate.exchange(
                            "https://kauth.kakao.com/oauth/token",
                            HttpMethod.POST,
                            kakaoTokenRequest,
                            String.class);
        } catch (Exception e) {
            throw new GlobalException(ErrorConstant.KAKAO_AUTH_ERROR);
        }

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new GlobalException(GlobalErrorCode.KAKAO_AUTH_ERROR);
        }
        return jsonNode.get("access_token").asText(); // 토큰 추출

        */
}
