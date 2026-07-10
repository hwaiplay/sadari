package org.our.sadari.sadariBook.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.sadariBook.dto.BookDto;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {

    // 독후감 저장, 조회, 수정, 삭제 로직은 서비스에 위임한다.
    private final BookService bookService;
    // 네이버 책 검색 호출과 응답 변환은 검색 서비스에 위임한다.
    private final BookSearchService bookSearchService;

    /**
     * 도서 검색
     * @Author SeungHyeon.Kang
     * @param query
     * @return
     */
    @GetMapping("/search")
    public ResultData searchBooks(
            @RequestParam("query") String query,
            @RequestParam(value = "start", defaultValue = "1") int start
    ) {
        // 컨트롤러는 검색 요청을 받고 결과 응답만 만든다.
        List<BookJsonDto.BookDto> books = bookSearchService.searchBooks(query, start);
        return ResultData.success(books);
    }

    /**
     * 독후감 리스트 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @return
     */
    @GetMapping("/getBookList")
    public ResultData getBookList(@AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자 번호로 해당 사용자의 독후감 목록만 조회한다.
        List<ReportDto> list = bookService.getBookList(userNumb);
        return ResultData.success(list);
    }

    /**
     * 독후감 상세 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param bookNumb
     * @return
     */
    @GetMapping("/getBookdetail/{bookNumb}")
    public ResultData getDetail(
            @AuthenticationPrincipal Long userNumb,
            @PathVariable("bookNumb") Long bookNumb
    ) {
        // 로그인 사용자 번호와 독후감 번호를 함께 전달해 소유자 기준으로 조회한다.
        ReportDto detail = bookService.getDetail(userNumb, bookNumb);

        if (StringUtil.isEmpty(detail)) {
            // 로그인 사용자가 접근 가능한 독후감이 없으면 조회 결과 없음으로 응답한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA); // 조회 결과가 없습니다.
        }

        log.debug("Book report detail lookup succeeded: {}", detail);
        return ResultData.success(detail);
    }

    /**
     * 도서 정보 상세 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    @GetMapping("/getBookInfo/{reportNumb}")
    public ResultData getBookInfo(
            @AuthenticationPrincipal Long userNumb,
            @PathVariable("reportNumb") Long reportNumb
    ) {
        // 독후감 소유자 조건으로 연결된 책 정보만 조회한다.
        BookDto bookInfo = bookService.getBookInfo(userNumb, reportNumb);

        if (StringUtil.isEmpty(bookInfo)) {
            // 독후감에 연결된 책 정보가 없거나 소유자가 다르면 조회 결과 없음으로 처리한다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA); // 조회 결과가 없습니다.
        }

        log.debug("Book info lookup succeeded: {}", bookInfo);
        return ResultData.success(bookInfo);
    }

    /**
     * 독후감 등록
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param requestDto
     * @return
     */
    @PostMapping("/setReport")
    public ResultData createReport(
            @AuthenticationPrincipal Long userNumb,
            @Valid @RequestBody ReportDto requestDto
    ) {
        if (hasInvalidBookFields(requestDto)) {
            // 독후감 등록은 새 책 연결 정보가 모두 있어야 하므로 책 필드 누락을 별도로 검사한다.
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST); // 요청값 검증 실패 응답이다.
        }

        // 요청 본문은 검증 후 서비스로 넘기고 사용자 번호는 서버에서 주입한다.
        ReportDto resultReportDto = bookService.setReport(userNumb, requestDto);

        if (StringUtil.isEmpty(resultReportDto.getReportNumb())) {
            // 저장 후 독후감 번호가 없으면 DB 저장이 완료되지 않은 상태로 판단한다.
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED); // 저장에 실패했어요. 다시 시도해주세요.
        }

        log.debug("Book report created: {}", resultReportDto.getReportNumb());
        return ResultData.success(resultReportDto.getReportNumb());
    }

    /**
     * 독후감 수정
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @param request
     * @return
     */
    @PutMapping("/uptReport/{reportNumb}")
    public ResultData uptReport(
            @AuthenticationPrincipal Long userNumb,
            @PathVariable("reportNumb") Long reportNumb,
            @Valid @RequestBody ReportDto request
    ) {
        // 경로의 독후감 번호와 로그인 사용자 번호를 기준으로 수정한다.
        if (StringUtil.isEmpty(reportNumb)) {
            // 수정 대상 독후감 번호가 없으면 어떤 글을 수정할지 확정할 수 없다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA); // 조회 결과가 없습니다.
        }

        ReportDto uptReport = bookService.uptReport(userNumb, reportNumb, request);

        log.debug("Book report updated: {}", uptReport);
        return ResultData.success(uptReport.getReportNumb());
    }

    /**
     * 독후감 삭제
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    @DeleteMapping("/delReport/{reportNumb}")
    public ResultData delReport(
            @AuthenticationPrincipal Long userNumb,
            @PathVariable("reportNumb") Long reportNumb
    ) {
        // 경로의 독후감 번호와 로그인 사용자 번호를 기준으로 삭제한다.
        if (StringUtil.isEmpty(reportNumb)) {
            // 삭제 대상 독후감 번호가 없으면 어떤 글을 삭제할지 확정할 수 없다.
            return ResultData.fail(ResultEnum.COMMON_NO_DATA); // 조회 결과가 없습니다.
        }

        if (bookService.delReport(userNumb, reportNumb) == 0) {
            // 삭제 건수가 0이면 대상이 없거나 사용자가 소유하지 않은 독후감이다.
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED); // 삭제에 실패했어요. 다시 시도해주세요.
        }

        log.debug("Book report deleted: {}", reportNumb);
        return ResultData.success();
    }

    /**
     * 등록 요청 도서 필수값 누락 여부 확인
     * @Author SeungHyeon.Kang
     * @param reportDto
     * @return
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
