package org.our.sadari.auth.vo;

import lombok.Data;
import java.util.Properties;

public class KakaoAccountVO {

    /*
    @sierrah
    [Kakao] 현재 mument 에서의 동의 항목
    필수 - 닉네임 (profile_nickname)
    필수 - 프로필사진 (profile_image_url)
    */
    public Long id; //카카오 아이디, *Required*
    public String connected_at; //서비스에 연결된 시각, UTC*
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
