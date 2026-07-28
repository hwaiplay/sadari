package org.our.sadari.push.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * fileName       : PushDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-25
 * description    : 푸시 알림 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-25        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Schema(description = "웹 푸시 설정과 구독 DTO 컨테이너", hidden = true)
public class PushDto {

    /**
     * 브라우저가 FCM token을 발급받기 위해 필요한 Firebase Web 설정 DTO이다.
     * VAPID 공개키만 브라우저에 제공하고 Firebase Admin 서비스 계정 정보는 노출하지 않는다.
     *
     * @author SeungHyeon.Kang
     */
    // 브라우저의 FCM 초기화에 필요한 공개 설정
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
     * TB_PSHSUB에 저장할 푸시 구독 DTO이다.
     * FCM 토큰은 ENDP_URLX 컬럼에 저장하고 p256dh와 auth 키는 일반 Web Push 전환을 위해 유지한다.
     *
     * @author SeungHyeon.Kang
     */
    // 사용자 브라우저의 푸시 구독 정보
    @Data
    @Schema(description = "푸시 구독 DTO")
    public static class PushSubDto {

        @Schema(description = "사용자 번호", example = "31", hidden = true)
        private Long userNumb;

        @Schema(description = "FCM token 또는 Web Push endpoint")
        private String endpUrlx;

        @Schema(description = "Web Push p256dh key")
        private String p256Keyx;

        @Schema(description = "Web Push auth key")
        private String authKeyx;

        @Schema(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"})
        private String useeYsno;
    }
}
