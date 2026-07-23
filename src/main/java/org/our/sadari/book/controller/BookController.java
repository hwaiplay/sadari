package org.our.sadari.book.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "도서", description = "도서 검색과 ISBN 기준 공개 평점 평균 조회 API")
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
    @Operation(summary = "도서 검색", description = "네이버 도서 API를 사용해 사용자가 입력한 검색어로 도서를 조회한다.")
    public ResultData searchBooks(@Parameter(description = "도서 검색어", example = "히가시노 게이고")
                                  @RequestParam("query") String query
                                , @Parameter(description = "네이버 검색 시작 위치", example = "1")
                                  @RequestParam(value = "start", defaultValue = "1") int start) {
        return bookSearchService.searchBooks(query, start);
    }

    /**
     * getRatingAverageByIsbn 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @return 처리 결과
     */
    @GetMapping("/ratingAverage/by-isbn")
    @Operation(summary = "ISBN 공개 평점 평균 조회", description = "공개/비공개 여부와 관계없이 해당 ISBN으로 작성된 독후감 평점 평균을 조회한다.")
    public ResultData getRatingAverageByIsbn(@Parameter(description = "평점 평균을 조회할 도서 ISBN", example = "9788972756194")
                                             @RequestParam("isbn") String isbn) {
        return reportService.getPublicRatingAverageByIsbn(isbn);
    }
}
