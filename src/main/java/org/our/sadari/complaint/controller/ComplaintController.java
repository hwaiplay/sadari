package org.our.sadari.complaint.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.complaint.service.ComplaintService;
import org.our.sadari.global.common.result.ResultData;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : ComplaintController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 인증 사용자의 콘텐츠 신고 접수 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/complaints")
@Tag(name = "신고", description = "사용자와 독후감 및 댓글 신고 접수 API")
public class ComplaintController {

    // 사용자 신고 접수 서비스
    private final ComplaintService complaintService;

    /**
     * 대상 유형별 실제 내용을 서버에서 확인해 신고 시점 스냅샷과 함께 접수한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param request 신고 대상과 사유
     * @return 접수된 신고 번호
     */
    @PostMapping
    @Operation(summary = "사용자 신고 접수", description = "대상 원문을 서버에서 조회해 변경되지 않는 신고 시점 스냅샷으로 저장한다.")
    public ResultData setComplaint(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                 , @Valid @RequestBody ComplaintCreateDto request) {

        // 인증 사용자의 대상 원문 스냅샷이 포함된 신고 접수 결과를 반환한다
        return complaintService.setComplaint(userNumb, request);
    }
}
