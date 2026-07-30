package org.our.sadari.popup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.popup.service.PopupContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : PopupContentController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 안내 팝업 콘텐츠 조회 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/popup-content")
@Tag(name = "팝업 콘텐츠", description = "사용자 화면의 안내 팝업 콘텐츠 조회 API")
public class PopupContentController {

    // 사용자 안내 팝업 콘텐츠 업무 처리 서비스
    private final PopupContentService popupContentService;

    /**
     * 사용 화면 구분과 팝업 코드에 해당하는 사용자 안내 콘텐츠를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 공통코드
     * @param popuCode 팝업 식별 코드
     * @return 사용자 안내 팝업 콘텐츠 조회 결과
     */
    @GetMapping
    @Operation(summary = "팝업 콘텐츠 조회", description = "사용 화면 구분과 팝업 코드로 사용자 안내 콘텐츠 한 건을 조회한다.")
    public ResultData getPopupContentDtl(
            @Parameter(description = "팝업 사용 화면 구분 공통코드", example = "ACCOUNT") @RequestParam String popuSitu
          , @Parameter(description = "팝업 식별 코드", example = "WITHDRAWAL_POLICY") @RequestParam String popuCode) {
        // 팝업 복합 식별값으로 사용자 안내 콘텐츠를 조회한 결과를 반환한다
        return popupContentService.getPopupContentDtl(popuSitu, popuCode);
    }
}
