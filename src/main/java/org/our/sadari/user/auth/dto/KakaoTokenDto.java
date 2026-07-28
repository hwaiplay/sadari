package org.our.sadari.user.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * fileName       : KakaoTokenDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 카카오 OAuth 토큰 API 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Data
@Schema(description = "Kakao OAuth 토큰 응답 DTO", hidden = true)
public class KakaoTokenDto {

    // 카카오 API 호출에 사용하는 액세스 토큰
    private String access_token;
    // OAuth 인증 스킴을 나타내는 토큰 유형
    private String token_type;
    // 카카오 액세스 토큰 재발급에 사용하는 리프레시 토큰
    private String refresh_token;
    // OpenID Connect 인증에 사용하는 ID 토큰
    private String id_token;
    // 액세스 토큰 만료까지 남은 초
    private int expires_in;
    // 리프레시 토큰 만료까지 남은 초
    private int refresh_token_expires_in;
    // 사용자에게 동의받은 OAuth 권한 범위
    private String scope;
}
