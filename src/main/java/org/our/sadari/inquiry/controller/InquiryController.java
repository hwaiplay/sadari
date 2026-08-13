package org.our.sadari.inquiry.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.inquiry.dto.InquiryCreateDto;
import org.our.sadari.inquiry.service.InquiryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : InquiryController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 사용자 고객문의 접수와 본인 문의 조회 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    현재 정지 이의제기 문의 조회 API 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class InquiryController {

    // 고객문의 업무 처리 서비스
    private final InquiryService inquiryService;

    /**
     * 현재 이용정지 이후 접수한 최신 이의제기 문의 번호를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 인증 사용자 번호
     * @return 현재 이용정지에 연결된 최신 문의 번호
     */
    @GetMapping("/suspension-appeal")
    public ResultData getSuspInquiryNumb(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {

        // 정지 안내 화면의 문의 버튼 이동에 사용할 본인 문의 번호를 반환한다
        return inquiryService.getSuspInquiryNumb(userNumb);
    }

    @GetMapping
    public ResultData getInquiryList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @RequestParam(defaultValue = "1") int page) {

        // 인증 사용자의 고객문의 목록을 반환한다
        return inquiryService.getInquiryList(userNumb, page);
    }

    @GetMapping("/{inqrNumb}")
    public ResultData getInquiryDtl(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @PathVariable Long inqrNumb) {

        // 인증 사용자가 작성한 고객문의 상세를 반환한다
        return inquiryService.getInquiryDtl(userNumb, inqrNumb);
    }

    @PostMapping
    public ResultData setInquiry(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @RequestBody InquiryCreateDto inquiryCreateDto) {

        // 인증 사용자의 새 고객문의를 접수하고 문의 번호를 반환한다
        return inquiryService.setInquiry(userNumb, inquiryCreateDto);
    }
}
