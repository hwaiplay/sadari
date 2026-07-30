package org.our.sadari.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.dto.UserWithdrawalDto;
import org.our.sadari.user.service.UserWithdrawalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : UserWithdrawalController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 계정 비활성화 및 영구 탈퇴 재인증과 영구 삭제 대기 취소 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 * 2026-07-29        SeungHyeon.Kang    환경별 영구 삭제 대기 설명 반영
 * 2026-07-30        SeungHyeon.Kang    사용자 계정 처리 용어를 비활성화와 영구 탈퇴로 정리
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/withdrawal")
@Tag(name = "계정 비활성화 및 탈퇴", description = "계정 비활성화와 영구 삭제 대기 API")
public class UserWithdrawalController {

    // 계정 비활성화 및 영구 탈퇴 업무 처리 서비스
    private final UserWithdrawalService userWithdrawalService;

    /**
     * 계정 처리 정책 입력값을 저장하고 Kakao 재인증 URL을 발급한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 회원 번호
     * @param request 계정 처리 유형과 필수 사유
     * @return Kakao 재인증 URL
     */
    @PostMapping("/reauth")
    @Operation(summary = "계정 처리 재인증 시작", description = "비활성화 또는 영구 탈퇴 유형과 사유를 검증하고 Kakao 재인증 URL을 발급한다.")
    public ResultData setWithdrawalRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @Valid @RequestBody UserWithdrawalDto request) {

        // 계정 처리 재인증 시작 결과를 반환한다
        return userWithdrawalService.setWithdrawalRequest(userNumb, request);
    }

    /**
     * 로그인 회원의 영구 삭제 예정일과 취소 가능 상태를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 회원 번호
     * @return 영구 삭제 대기 정보
     */
    @GetMapping("/status")
    @Operation(summary = "영구 탈퇴 상태 조회", description = "영구 삭제 대기 회원의 삭제 예정일을 조회한다.")
    public ResultData getWithdrawalStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {

        // 영구 삭제 대기 상태 조회 결과를 반환한다
        return userWithdrawalService.getWithdrawalStatus(userNumb);
    }

    /**
     * 영구 삭제 대기를 취소하고 회원 상태를 정상으로 복구한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 회원 번호
     * @return 복구 처리 결과
     */
    @PostMapping("/cancel")
    @Operation(summary = "영구 탈퇴 취소", description = "설정된 영구 삭제 대기를 취소하고 회원 상태를 복구한다.")
    public ResultData uptWithdrawalCancel(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {

        // 영구 삭제 대기 취소 결과를 반환한다
        return userWithdrawalService.uptWithdrawalCancel(userNumb);
    }
}
