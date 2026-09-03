package org.our.sadari.serviceinfo.controller;

import io.swagger.v3.oas.annotations.Parameter;
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
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-info")
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
    public ResultData getServiceInfoList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 인증 사용자의 서비스 정보 조회 결과를 반환함
        return serviceInfoService.getServiceInfoList(userNumb);
    }
}
