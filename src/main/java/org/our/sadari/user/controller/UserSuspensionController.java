package org.our.sadari.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.service.UserSuspensionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : UserSuspensionController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 정지 회원에게 제한적으로 공개할 현재 정지 상태 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/suspension")
@Tag(name = "회원 정지", description = "로그인 회원의 현재 이용 정지 상태 조회 API")
public class UserSuspensionController {

    // 사용자 정지 상태 조회 서비스
    private final UserSuspensionService userSuspensionService;

    /**
     * 로그인 회원의 현재 정지 유형과 사유 및 기간을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 회원 번호
     * @return 현재 정지 상태
     */
    @GetMapping
    @Operation(summary = "회원 정지 상태 조회", description = "내부 관리자 메모를 제외한 현재 정지 유형과 사유 및 기간을 조회한다.")
    public ResultData getUserSuspension(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {

        // 로그인 회원 본인의 현재 정지 상태를 반환한다
        return userSuspensionService.getUserSuspension(userNumb);
    }
}
