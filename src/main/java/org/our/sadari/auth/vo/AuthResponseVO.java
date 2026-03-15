package org.our.sadari.auth.vo;

import lombok.*;

public class AuthResponseVO {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class LoginResponse {
        private Long id;
        private String name;
        private AuthTokens token;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AuthTokens {
        private String accessToken;
        private String refreshToken;
    }
}
