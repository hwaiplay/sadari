package org.our.sadari.push.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Web Push/FCM 구독과 Firebase Web 설정 응답에 사용하는 DTO 묶음입니다.
 * TB_PSHSUB는 일반 Web Push 컬럼명을 가지고 있지만, 현재 구현은 FCM registration token을 ENDP_URLX에 저장합니다.
 *
 * @author Seunghyeon.Kang
 */
public class PushDto {

    /**
     * 브라우저가 FCM token을 발급받기 위해 필요한 Firebase Web 설정 DTO입니다.
     * VAPID public key는 공개 가능한 값이지만, Firebase Admin service account 정보는 절대 프론트로 내려주지 않습니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "Firebase Web Push 설정 DTO")
    public static class FirebaseWebConfigDto {

        @Schema(description = "Firebase Web API Key")
        private String apiKey;

        @Schema(description = "Firebase Auth Domain")
        private String authDomain;

        @Schema(description = "Firebase Project ID")
        private String projectId;

        @Schema(description = "Firebase Storage Bucket")
        private String storageBucket;

        @Schema(description = "Firebase Messaging Sender ID")
        private String messagingSenderId;

        @Schema(description = "Firebase App ID")
        private String appId;

        @Schema(description = "Web Push VAPID Public Key")
        private String vapidPublicKey;
    }

    /**
     * TB_PSHSUB에 저장할 푸시 구독 DTO입니다.
     * FCM token은 긴 문자열이므로 기존 ENDP_URLX 컬럼에 저장하고, p256/auth 키는 일반 Web Push 전환 시 사용할 수 있게 남겨둡니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "푸시 구독 DTO")
    public static class PushSubDto {

        // 구독을 소유한 로그인 사용자 번호입니다.
        @Schema(description = "사용자 번호", example = "31", hidden = true)
        private Long userNumb;

        // 현재 구현에서는 FCM registration token을 저장합니다.
        @Schema(description = "FCM token 또는 Web Push endpoint")
        private String endpUrlx;

        // 일반 Web Push 구독 방식으로 전환할 때 사용할 p256dh key입니다.
        @Schema(description = "Web Push p256dh key")
        private String p256Keyx;

        // 일반 Web Push 구독 방식으로 전환할 때 사용할 auth key입니다.
        @Schema(description = "Web Push auth key")
        private String authKeyx;

        // 구독 사용 여부입니다.
        @Schema(description = "사용 여부", example = "Y")
        private String useeYsno;
    }
}
