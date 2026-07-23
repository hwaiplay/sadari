package org.our.sadari.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.report.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 독후감 화면에서 사용하는 목록, 상세, 등록, 수정, 삭제, 공개 독후감, 좋아요 API를 제공한다.
 * 실제 업무 검증과 트랜잭션 처리는 ReportService로 위임하고, 이 클래스는 요청 파라미터 바인딩과 응답 전달만 담당한다.
 *
 * @author Seunghyeon.Kang
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
@Tag(name = "독후감", description = "독후감 목록, 상세, 등록, 수정, 삭제, 공개 독후감, 좋아요 API")
public class ReportController {

    private final ReportService reportService;

    /**
     * 로그인 사용자의 독후감 목록을 검색어와 정렬 조건에 따라 조회한다.
     * bookKeyword는 책 제목과 작가명 검색에 사용하고, sortType이 없으면 종료일 내림차순을 기본값으로 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param bookKeyword 책 제목 또는 작가명 검색어
     * @param sortType 목록 정렬 유형
     * @return 독후감 목록 조회 결과
     */
    @GetMapping("/getBookList")
    @Operation(summary = "내 독후감 목록 조회", description = "로그인 사용자의 독후감을 책 제목 또는 작가명 검색어와 정렬 조건으로 조회한다.")
    public ResultData getBookList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                , @Parameter(description = "책 제목 또는 작가명 검색어", example = "용의자")
                                  @RequestParam(value = "bookKeyword", required = false) String bookKeyword
                                , @Parameter(description = "정렬 유형", example = Constant.SORT_END_DATE_DESC)
                                  @RequestParam(value = "sortType", defaultValue = Constant.SORT_END_DATE_DESC) String sortType) {
        return reportService.getBookList(userNumb, bookKeyword, sortType);
    }

    /**
     * 로그인 사용자가 작성한 독후감 상세 정보와 연결된 도서 정보를 함께 조회한다.
     * 화면에서는 같은 URL 안에서 독후감 영역과 도서 정보 영역을 전환해 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param bookNumb 상세 조회할 독후감 번호
     * @return 독후감 상세 조회 결과
     */
    @GetMapping("/getBookdetail/{bookNumb}")
    @Operation(summary = "내 독후감 상세 조회", description = "로그인 사용자가 작성한 독후감과 연결된 도서 정보를 함께 조회한다.")
    public ResultData getDetail(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                              , @Parameter(description = "독후감 번호", example = "1")
                                @PathVariable("bookNumb") Long bookNumb) {
        return reportService.getDetail(userNumb, bookNumb);
    }

    /**
     * ISBN을 기준으로 다른 사용자가 공개한 독후감 목록을 조회한다.
     * 좋아요 여부와 좋아요 수 표시를 위해 로그인 사용자 번호를 함께 전달한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param isbn 공개 독후감을 조회할 도서 ISBN
     * @return 공개 독후감 목록 조회 결과
     */
    @GetMapping("/publicReports/by-isbn")
    @Operation(summary = "ISBN 공개 독후감 목록 조회", description = "해당 ISBN 도서에 대해 다른 사용자가 공개한 독후감 목록을 조회한다.")
    public ResultData getPublicReportsByIsbn(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                           , @Parameter(description = "공개 독후감을 조회할 도서 ISBN", example = "9788972756194")
                                             @RequestParam("isbn") String isbn) {
        return reportService.getPublicReportsByIsbn(userNumb, isbn);
    }

    /**
     * 공개 독후감의 좋아요 상태를 토글한다.
     * 이미 좋아요를 누른 경우에는 취소하고, 누르지 않은 경우에는 신규 좋아요를 등록한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reportNumb 좋아요를 토글할 독후감 번호
     * @return 변경 후 좋아요 상태와 좋아요 수
     */
    @PostMapping("/publicReports/{reportNumb}/like")
    @Operation(summary = "공개 독후감 좋아요 토글", description = "공개 독후감의 좋아요를 등록하거나 취소하고 변경된 좋아요 상태를 반환한다.")
    public ResultData setReportLike(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                  , @Parameter(description = "좋아요를 토글할 독후감 번호", example = "1")
                                    @PathVariable("reportNumb") Long reportNumb) {
        return reportService.setReportLike(userNumb, reportNumb);
    }

    /**
     * 새 독후감과 필요 시 신규 도서 정보를 함께 등록한다.
     * DTO 검증은 Controller에서 1차 수행하고, 업무 규칙 검증은 Service에서 한 번 더 수행한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param requestDto 등록할 독후감과 도서 정보
     * @return 등록된 독후감 번호
     */
    @PostMapping("/setReport")
    @Operation(summary = "독후감 등록", description = "도서 정보가 없으면 도서를 먼저 저장한 뒤 로그인 사용자의 독후감을 등록한다.")
    public ResultData createReport(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                 , @Valid @RequestBody ReportDto requestDto) {
        return reportService.setReport(userNumb, requestDto);
    }

    /**
     * 기존 독후감 정보를 수정한다.
     * URL의 reportNumb를 기준으로 수정 대상을 확정하고, 본문 DTO에는 변경할 독후감 값을 담는다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reportNumb 수정할 독후감 번호
     * @param request 수정할 독후감 정보
     * @return 수정된 독후감 번호
     */
    @PutMapping("/uptReport/{reportNumb}")
    @Operation(summary = "독후감 수정", description = "기존 독후감의 도서, 기간, 상태, 별점, 공개 여부, 본문을 수정한다.")
    public ResultData uptReport(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                              , @Parameter(description = "수정할 독후감 번호", example = "1")
                                @PathVariable("reportNumb") Long reportNumb
                              , @Valid @RequestBody ReportDto request) {
        return reportService.uptReport(userNumb, reportNumb, request);
    }

    /**
     * 마이페이지의 현재 읽고 있는 책 목록에서 독서 상태와 별점만 빠르게 수정한다.
     * 본문, 기간, 공개 여부 등 전체 독후감 수정 화면에서 다루는 값은 변경하지 않도록 별도 API로 분리한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reportNumb 수정할 독후감 번호
     * @param request 수정할 독서 상태와 별점 정보
     * @return 수정 처리 결과
     */
    @PutMapping("/uptReport/status-grade/{reportNumb}")
    @Operation(summary = "독서 상태와 별점 빠른 수정", description = "마이페이지 팝업에서 독서 상태와 별점만 빠르게 수정한다.")
    public ResultData uptReportStatusGrade(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                          , @Parameter(description = "수정할 독후감 번호", example = "1")
                                            @PathVariable("reportNumb") Long reportNumb
                                          , @RequestBody ReportDto request) {
        return reportService.uptReportStatusGrade(userNumb, reportNumb, request);
    }

    /**
     * 로그인 사용자가 작성한 독후감을 삭제한다.
     * Service에서 사용자 번호와 독후감 번호를 함께 조건으로 사용해 본인 데이터만 삭제되도록 한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reportNumb 삭제할 독후감 번호
     * @return 삭제 처리 결과
     */
    @DeleteMapping("/delReport/{reportNumb}")
    @Operation(summary = "독후감 삭제", description = "로그인 사용자가 작성한 독후감을 삭제한다.")
    public ResultData delReport(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                              , @Parameter(description = "삭제할 독후감 번호", example = "1")
                                @PathVariable("reportNumb") Long reportNumb) {
        return reportService.delReport(userNumb, reportNumb);
    }
}
