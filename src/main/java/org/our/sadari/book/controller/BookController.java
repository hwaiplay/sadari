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
 * fileName       : BookController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 도서 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
@Tag(name = "도서", description = "도서 검색과 ISBN 기준 공개 평점 평균 조회 API")
public class BookController {
    // BookSearch 업무 처리 서비스
    private final BookSearchService bookSearchService;
    // Report 업무 처리 서비스
    private final ReportService reportService;

    /**
     * 검색어와 검색 시작 위치를 사용하여 네이버 도서 API의 도서 목록을 검색한다.
     *
     * @author SeungHyeon.Kang
     * @return 검색된 도서 목록
     */
    @GetMapping("/search")
    @Operation(summary = "도서 검색", description = "네이버 도서 API를 사용해 사용자가 입력한 검색어로 도서를 조회한다.")
    public ResultData searchBooks(@Parameter(description = "도서 검색어", example = "히가시노 게이고")@RequestParam("query") String query
                                , @Parameter(description = "네이버 검색 시작 위치", example = "1")@RequestParam(value = "start", defaultValue = "1") int start) {
        // 검색어와 검색 시작 위치를 사용하여 네이버 도서 API의 도서 목록을 검색 결과를 반환한다
        return bookSearchService.searchBooks(query, start);
    }

    /**
     * ISBN 기준 도서 평균 평점 조회한다.
     *
     * @author SeungHyeon.Kang
     * @return 처리 결과
     */
    @GetMapping("/ratingAverage/by-isbn")
    @Operation(summary = "ISBN 공개 평점 평균 조회", description = "공개/비공개 여부와 관계없이 해당 ISBN으로 작성된 독후감 평점 평균을 조회한다.")
    public ResultData getRatingAverageByIsbn(@Parameter(description = "평점 평균을 조회할 도서 ISBN", example = "9788972756194")@RequestParam("isbn") String isbn) {
        // ISBN 기준 도서 평균 평점 조회 결과를 반환한다
        return reportService.getPublicRatingAverageByIsbn(isbn);
    }
}
