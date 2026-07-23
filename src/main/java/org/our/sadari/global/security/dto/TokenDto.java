package org.our.sadari.global.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * TokenDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
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

    public static TokenDto of(String accessToken, String refreshToken) {
        return TokenDto
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
