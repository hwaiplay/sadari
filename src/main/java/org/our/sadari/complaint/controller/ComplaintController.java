package org.our.sadari.complaint.controller;

import io.swagger.v3.oas.annotations.Parameter;
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
 * author         : HanWon.Jang
 * date           : 2026-08-21
 * description    : 사용자 콘텐츠 신고 접수 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-21        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/complaints")
public class ComplaintController {

    // 사용자 콘텐츠 신고 접수 서비스
    private final ComplaintService complaintService;

    /**
     * 인증 사용자의 독후감 또는 댓글 신고를 접수한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @param request 신고 대상과 사유 입력값
     * @return 접수된 신고 번호 응답
     */
    @PostMapping
    public ResultData setComplaint(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @Valid @RequestBody ComplaintCreateDto request) {

        // 인증 사용자와 검증된 신고 입력값을 신고 서비스에 전달한다
        return complaintService.setComplaint(userNumb, request);
    }
}
