package org.our.sadari.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * fileName       : TokenDto
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Getter
@Builder
@AllArgsConstructor
public class TokenDto {

    private String accessToken;
    private String refreshToken;

    public static TokenDto of(String accessToken, String refreshToken) {
        return TokenDto
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
