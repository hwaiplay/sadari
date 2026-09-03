package org.our.sadari.welcome.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.welcome.service.WelcomePageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : WelcomePageController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-28
 * description    : 사용자 웰컴 화면의 현재 배포 페이지 조회 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/welcome-pages")
public class WelcomePageController {

    // 사용자 웰컴페이지 조회 서비스
    private final WelcomePageService welcomePageService;

    /** 현재 배포 중인 관리자 웰컴페이지 목록을 조회함 */
    @GetMapping
    public ResultData getWelcomePageList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 인증 사용자의 계정 상태를 검증한 웰컴페이지 목록을 반환함
        return welcomePageService.getWelcomePageList(userNumb);
    }
}
