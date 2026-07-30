package org.our.sadari.global.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * fileName       : TokenDto
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : JWT 액세스 토큰과 리프레시 토큰을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 * 2026-07-30        SeungHyeon.Kang    비활성화 계정 복귀 여부 추가
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "JWT 토큰 DTO")
public class TokenDto {

    @Schema(description = "Access Token")
    private String accessToken;

    @Schema(description = "Refresh Token")
    private String refreshToken;

    @Schema(description = "이번 로그인에서 비활성화 계정을 다시 활성화했는지 여부")
    private boolean accountReactivated;

    /**
     * Access Token과 Refresh Token을 포함한 토큰 DTO를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param accessToken API 인증에 사용할 Access Token
     * @param refreshToken Access Token 재발급에 사용할 Refresh Token
     * @param accountReactivated 이번 로그인에서 비활성화 계정을 다시 활성화했는지 여부
     * @return 발급 토큰과 계정 복귀 여부
     */
    public static TokenDto of(String accessToken, String refreshToken, boolean accountReactivated) {
        // 발급한 토큰과 이번 로그인에서 발생한 계정 복귀 여부를 담은 인증 응답을 반환한다
        return TokenDto
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accountReactivated(accountReactivated)
                .build();
    }
}
