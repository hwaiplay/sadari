package org.our.sadari.push.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.push.dto.PushDto;
import org.our.sadari.push.service.PushService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : PushController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-25
 * description    : 푸시 알림 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-25        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
@Tag(name = "푸시", description = "PWA 푸시 알림 설정 및 구독 API")
public class PushController {
    // Push 업무 처리 서비스
    private final PushService pushService;

    /**
     * 프론트에서 Firebase Messaging을 초기화할 공개 설정을 조회한다.
     * service account json 경로나 private key는 절대 응답하지 않다.
     *
     * @author SeungHyeon.Kang
     * @return Firebase Web 공개 설정
     */
    @GetMapping("/config")
    @Operation(summary = "Firebase Web 설정 조회", description = "브라우저에서 FCM token을 발급받기 위한 공개 설정을 조회한다.")
    public ResultData getFirebaseWebConfig() {
        // 프론트에서 Firebase Messaging을 초기화할 공개 설정을 조회한 결과를 반환한다
        return pushService.getFirebaseWebConfig();
    }

    /**
     * 로그인 사용자의 현재 브라우저 FCM token을 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param request FCM token 요청
     * @return 저장 결과
     */
    @PostMapping("/subscribe")
    @Operation(summary = "푸시 구독 저장", description = "로그인 사용자의 현재 브라우저 FCM token을 TB_PSHSUB에 저장한다.")
    public ResultData setPushSub(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                               , @RequestBody PushDto.PushSubDto request) {
        // 로그인 사용자의 현재 브라우저 FCM token을 저장한 결과를 반환한다
        return pushService.setPushSub(loginUserNumb, request);
    }

    /**
     * 로그인 사용자의 현재 브라우저 FCM token을 비활성화한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param request FCM token 요청
     * @return 비활성화 결과
     */
    @DeleteMapping("/subscribe")
    @Operation(summary = "푸시 구독 해제", description = "로그인 사용자의 현재 브라우저 FCM token을 비활성화한다.")
    public ResultData delPushSub(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                               , @RequestBody PushDto.PushSubDto request) {
        // 로그인 사용자의 현재 브라우저 FCM token을 비활성화한 결과를 반환한다
        return pushService.delPushSub(loginUserNumb, request);
    }
}
