package org.our.sadari.sadariBook.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.sadariBook.dto.BookJsonDto;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.service.BookSearchService;
import org.our.sadari.sadariBook.service.BookService;
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
 * 책 검색, 독후감 등록/조회/수정/삭제, 공개 독후감 조회 API를 제공하는 컨트롤러입니다.
 *
 * @author Seunghyeon.Kang
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;
    private final BookSearchService bookSearchService;

    /**
     * Naver 책 검색 API를 통해 책 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param query 검색어
     * @param start 검색 시작 위치
     * @return 검색된 책 목록을 담은 공통 응답
     */
    @GetMapping("/search")
    public ResultData searchBooks(@RequestParam("query") String query
                                , @RequestParam(value = "start", defaultValue = "1") int start) {
        List<BookJsonDto.BookDto> books = bookSearchService.searchBooks(query, start);
        return ResultData.success(books);
    }

    /**
     * 로그인한 회원의 독후감 목록을 검색어와 정렬 조건에 맞춰 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param bookKeyword 책 제목 또는 작가 검색어
     * @param sortType 목록 정렬 코드
     * @return 독후감 목록을 담은 공통 응답
     */
    @GetMapping("/getBookList")
    public ResultData getBookList(@AuthenticationPrincipal Long userNumb
                                , @RequestParam(value = "bookKeyword", required = false) String bookKeyword
                                , @RequestParam(value = "sortType", defaultValue = Constant.SORT_END_DATE_DESC) String sortType) {
        List<ReportDto> list = bookService.getBookList(userNumb, bookKeyword, sortType);
        return ResultData.success(list);
    }

    /**
     * 로그인한 회원이 작성한 독후감 상세 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param bookNumb 조회할 독후감 번호
     * @return 독후감 상세 정보를 담은 공통 응답
     */
    @GetMapping("/getBookdetail/{bookNumb}")
    public ResultData getDetail(@AuthenticationPrincipal Long userNumb
                              , @PathVariable("bookNumb") Long bookNumb) {
        ReportDto detail = bookService.getDetail(userNumb, bookNumb);

        // 소유권 조건까지 포함해 조회하므로 결과가 없으면 존재하지 않거나 접근할 수 없는 독후감입니다.
        if (StringUtil.isEmpty(detail)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        log.debug("Book report detail lookup succeeded: {}", detail);
        return ResultData.success(detail);
    }

    /**
     * ISBN을 기준으로 공개 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param isbn 조회할 ISBN
     * @return 공개 독후감 목록을 담은 공통 응답
     */
    @GetMapping("/publicReports/by-isbn")
    public ResultData getPublicReportsByIsbn(@AuthenticationPrincipal Long userNumb
                                           , @RequestParam("isbn") String isbn) {
        if (StringUtil.isEmpty(isbn)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        return ResultData.success(bookService.getPublicReportsByIsbn(userNumb, isbn));
    }

    /**
     * ISBN을 기준으로 전체 독후감 평균 별점을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param isbn 평균 별점을 조회할 ISBN
     * @return 평균 별점을 담은 공통 응답
     */
    @GetMapping("/ratingAverage/by-isbn")
    public ResultData getRatingAverageByIsbn(@RequestParam("isbn") String isbn) {
        if (StringUtil.isEmpty(isbn)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        BigDecimal average = bookService.getPublicRatingAverageByIsbn(isbn);
        return ResultData.success(average);
    }

    /**
     * 독후감 좋아요 상태를 토글합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 좋아요 대상 독후감 번호
     * @return 변경된 좋아요 상태를 담은 공통 응답
     */
    @PostMapping("/publicReports/{reportNumb}/like")
    public ResultData setReportLike(@AuthenticationPrincipal Long userNumb
                                  , @PathVariable("reportNumb") Long reportNumb) {
        if (StringUtil.isEmpty(reportNumb)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        return ResultData.success(bookService.setReportLike(userNumb, reportNumb));
    }

    /**
     * 독후감을 신규 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param requestDto 등록할 독후감 정보
     * @return 생성된 독후감 번호를 담은 공통 응답
     */
    @PostMapping("/setReport")
    public ResultData createReport(@AuthenticationPrincipal Long userNumb
                                 , @Valid @RequestBody ReportDto requestDto) {
        // 책 정보는 독후감 등록 시 함께 저장되므로 필수 책 정보가 없으면 등록을 거부합니다.
        if (hasInvalidBookFields(requestDto)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        ReportDto resultReportDto = bookService.setReport(userNumb, requestDto);

        if (StringUtil.isEmpty(resultReportDto.getReportNumb())) {
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        log.debug("Book report created: {}", resultReportDto.getReportNumb());
        return ResultData.success(resultReportDto.getReportNumb());
    }

    /**
     * 독후감을 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 수정할 독후감 번호
     * @param request 수정할 독후감 정보
     * @return 수정된 독후감 번호를 담은 공통 응답
     */
    @PutMapping("/uptReport/{reportNumb}")
    public ResultData uptReport(@AuthenticationPrincipal Long userNumb
                              , @PathVariable("reportNumb") Long reportNumb
                              , @Valid @RequestBody ReportDto request) {
        if (StringUtil.isEmpty(reportNumb)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        ReportDto uptReport = bookService.uptReport(userNumb, reportNumb, request);

        log.debug("Book report updated: {}", uptReport);
        return ResultData.success(uptReport.getReportNumb());
    }

    /**
     * 독후감을 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 삭제할 독후감 번호
     * @return 삭제 결과 공통 응답
     */
    @DeleteMapping("/delReport/{reportNumb}")
    public ResultData delReport(@AuthenticationPrincipal Long userNumb
                              , @PathVariable("reportNumb") Long reportNumb) {
        if (StringUtil.isEmpty(reportNumb)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 삭제 건수가 0이면 존재하지 않거나 현재 회원 소유가 아닌 독후감입니다.
        if (bookService.delReport(userNumb, reportNumb) == 0) {
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED);
        }

        log.debug("Book report deleted: {}", reportNumb);
        return ResultData.success();
    }

    /**
     * 독후감 등록 시 함께 저장되어야 하는 책 기본 정보가 비어 있는지 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검사할 독후감 요청 DTO
     * @return 필수 책 정보가 하나라도 비어 있으면 true
     */
    private boolean hasInvalidBookFields(ReportDto reportDto) {
        return StringUtil.hasEmpty(
                reportDto.getBookTitl(),
                reportDto.getBookAthr(),
                reportDto.getBookPubl(),
                reportDto.getBookIsbn(),
                reportDto.getBookCvim(),
                reportDto.getBookDesc()
        );
    }
}
