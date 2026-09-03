package org.our.sadari.user.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.util.Properties;

/**
 * fileName       : KakaoAccountDto
 * author         : SeungHyeon.Kang
 * date           : 2026-03-16
 * description    : 카카오 사용자 정보 API의 계정과 프로필 데이터를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-16        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Schema(description = "Kakao 사용자 계정 응답 DTO", hidden = true)
public class KakaoAccountDto {

    // 카카오 사용자 식별자
    public Long id;
    // ISO-8601 형식의 카카오 계정 연결 일시
    public String connected_at;
    // 카카오 사용자 속성 Map
    public Properties properties;
    // 카카오 계정 동의 및 프로필 정보
    public KakaoAccount kakao_account;

    /**
     * 카카오 계정의 사용자 동의 상태와 프로필 정보를 전달함
     *
     * @author SeungHyeon.Kang
     */
    // 카카오 계정 동의 상태와 프로필 정보
    @Data
    @Schema(description = "카카오 계정 동의 상태와 프로필 DTO", hidden = true)
    public static class KakaoAccount {

        // 프로필 닉네임 제공 동의 필요 여부
        public Boolean profile_nickname_needs_agreement;
        // 프로필 이미지 제공 동의 필요 여부
        public Boolean profile_image_needs_agreement;
        // 이메일 제공 동의 필요 여부
        public Boolean email_needs_agreement;
        // 카카오 계정 이메일 유효 여부
        public Boolean is_email_valid;
        // 카카오 계정 이메일 인증 여부
        public Boolean is_email_verified;
        // 카카오 계정 이메일 보유 여부
        public Boolean has_email;

        // 카카오 계정 이메일 주소
        public String email;
        // 카카오 프로필 정보
        public KakaoProfile profile;

        /**
         * 카카오 프로필의 닉네임과 이미지 정보를 전달함
         *
         * @author SeungHyeon.Kang
         */
        // 카카오 프로필 닉네임과 이미지 정보
        @Data
        @Schema(description = "카카오 프로필 응답 DTO", hidden = true)
        public static class KakaoProfile {

            // 카카오 프로필 닉네임
            public String nickname;
            // 카카오 프로필 썸네일 이미지 URL
            public String thumbnail_image_url;
            // 카카오 프로필 원본 이미지 URL
            public String profile_image_url;
            // 카카오 기본 프로필 이미지 사용 여부
            public boolean is_default_image;
            // 카카오 기본 닉네임 사용 여부
            public boolean is_default_nickname;
        }
    }
}
