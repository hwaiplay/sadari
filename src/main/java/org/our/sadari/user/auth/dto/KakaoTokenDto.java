package org.our.sadari.user.auth.dto;

import lombok.Data;

@Data
public class KakaoTokenDto {
    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */

    private String access_token;
    private String token_type;
    private String refresh_token;
    private String id_token;
    private int expires_in;
    private int refresh_token_expires_in;
    private String scope;
}
