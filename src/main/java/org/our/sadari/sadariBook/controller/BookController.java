package org.our.sadari.sadariBook.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {

    // 독후감 저장, 조회, 수정, 삭제 로직은 서비스에 위임한다.
    private final BookService bookService;
    // 네이버 책 검색 호출과 응답 변환은 검색 서비스에 위임한다.
    private final BookSearchService bookSearchService;

    @GetMapping("/search")
    public ResultData searchBooks(@RequestParam("query") String query) {
        // 컨트롤러는 검색 요청을 받고 결과 응답만 만든다.
        List<BookJsonDto.BookDto> books = bookSearchService.searchBooks(query);
        return ResultData.success(books);
    }

    @GetMapping("/getBookList")
    public ResultData getBookList(@AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자 번호로 해당 사용자의 독후감 목록만 조회한다.
        List<ReportDto> list = bookService.getBookList(userNumb);
        return ResultData.success(list);
    }

    @GetMapping("/getBookdetail/{bookNumb}")
    public ResultData getDetail(
            @AuthenticationPrincipal Long userNumb,
            @PathVariable("bookNumb") Long bookNumb
    ) {
        // 로그인 사용자 번호와 독후감 번호를 함께 전달해 소유자 기준으로 조회한다.
        ReportDto detail = bookService.getDetail(userNumb, bookNumb);

        if (StringUtil.isEmpty(detail)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        log.debug("Book report detail lookup succeeded: {}", detail);
        return ResultData.success(detail);
    }

    @PostMapping("/setReport")
    public ResultData createReport(
            @AuthenticationPrincipal Long userNumb,
            @Valid @RequestBody ReportDto requestDto
    ) {
        // 요청 본문은 검증 후 서비스로 넘기고 사용자 번호는 서버에서 주입한다.
        ReportDto resultReportDto = bookService.setReport(userNumb, requestDto);

        if (StringUtil.isEmpty(resultReportDto.getReportNumb())) {
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        log.debug("Book report created: {}", resultReportDto.getReportNumb());
        return ResultData.success(resultReportDto.getReportNumb());
    }

    @PutMapping("/uptReport/{reportNumb}")
    public ResultData uptReport(
            @AuthenticationPrincipal Long userNumb,
            @PathVariable("reportNumb") Long reportNumb,
            @Valid @RequestBody ReportDto request
    ) {
        // 경로의 독후감 번호와 로그인 사용자 번호를 기준으로 수정한다.
        if (StringUtil.isEmpty(reportNumb)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        ReportDto uptReport = bookService.uptReport(userNumb, reportNumb, request);

        log.debug("Book report updated: {}", uptReport);
        return ResultData.success(uptReport.getReportNumb());
    }

    @DeleteMapping("/delReport/{reportNumb}")
    public ResultData delReport(
            @AuthenticationPrincipal Long userNumb,
            @PathVariable("reportNumb") Long reportNumb
    ) {
        // 경로의 독후감 번호와 로그인 사용자 번호를 기준으로 삭제한다.
        if (StringUtil.isEmpty(reportNumb)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        if (bookService.delReport(userNumb, reportNumb) == 0) {
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED);
        }

        log.debug("Book report deleted: {}", reportNumb);
        return ResultData.success();
    }
}
