package org.our.sadari.book.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.book.service.BookSearchService;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BookController 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {

    private final BookSearchService bookSearchService;
    private final ReportService reportService;

    /**
     * searchBooks 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @return 처리 결과
     */
    @GetMapping("/search")
    public ResultData searchBooks(@RequestParam("query") String query
                                , @RequestParam(value = "start", defaultValue = "1") int start) {
        return bookSearchService.searchBooks(query, start);
    }

    /**
     * getRatingAverageByIsbn 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @return 처리 결과
     */
    @GetMapping("/ratingAverage/by-isbn")
    public ResultData getRatingAverageByIsbn(@RequestParam("isbn") String isbn) {
        return reportService.getPublicRatingAverageByIsbn(isbn);
    }
}
