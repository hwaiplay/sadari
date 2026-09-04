package org.our.sadari.serviceinfo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.serviceinfo.service.ServiceInfoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : ServiceInfoController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 활성 사용자의 서비스 정보 카테고리와 현재 배포본 조회 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 * 2026-09-04        SeungHyeon.Kang    개인정보처리방침 공개 조회 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-info")
@Tag(name = "서비스 정보", description = "서비스 정책과 개인정보처리방침 배포본 조회 API")
public class ServiceInfoController {

    // 사용자 서비스 정보 조회 서비스
    private final ServiceInfoService serviceInfoService;

    /**
     * 서비스 정보 카테고리와 현재 배포 버전을 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 접근하는 인증 사용자 번호
     * @return 사용자에게 노출할 서비스 정보 카테고리와 현재 배포본
     */
    @GetMapping
    @Operation(summary = "서비스 정보 목록 조회", description = "활성 사용자에게 서비스 정보 카테고리와 현재 배포본을 제공한다.")
    public ResultData getServiceInfoList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 인증 사용자의 서비스 정보 조회 결과를 반환함
        return serviceInfoService.getServiceInfoList(userNumb);
    }

    /**
     * 로그인 전에 확인할 현재 배포 개인정보처리방침을 조회함
     *
     * @author SeungHyeon.Kang
     * @return 현재 배포된 개인정보처리방침
     */
    @GetMapping("/privacy-policy")
    @Operation(summary = "개인정보처리방침 공개 조회", description = "인증 없이 현재 배포된 개인정보처리방침 한 건을 제공한다.")
    public ResultData getPrivacyPolicy() {
        // 공개 범위를 개인정보처리방침 현재 배포본으로 제한한 조회 결과를 반환함
        return serviceInfoService.getPrivacyPolicy();
    }
}
