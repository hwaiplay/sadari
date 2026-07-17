package org.our.sadari.user.auth.dto;

import lombok.Data;
import java.util.Properties;

/**
 * KakaoAccountDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
public class KakaoAccountDto {

    public Long id; // 업무 규칙에서 사용하는 고정 설정 값이다.
    public String connected_at; // 업무 규칙에서 사용하는 고정 설정 값이다.
    public Properties properties;
    public KakaoAccount kakao_account;

    @Data
    public class KakaoAccount {
        public Boolean profile_nickname_needs_agreement;
        public Boolean profile_image_needs_agreement;
        public Boolean email_needs_agreement;
        public Boolean is_email_valid;
        public Boolean is_email_verified;
        public Boolean has_email;

        public String email;
        public KakaoProfile profile;

        @Data
        public class KakaoProfile {
            public String nickname;
            public String thumbnail_image_url;
            public String profile_image_url;
            public boolean is_default_image;
            public boolean is_default_nickname;
        }
    }
}
