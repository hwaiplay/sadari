package org.our.sadari.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * fileName       : PopularSearchKeywordDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-16
 * description    : 최근 도서 인기 검색어의 순위와 화면 표시값을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-16        SeungHyeon.Kang    최초 생성
 */
@Getter
@AllArgsConstructor
@Schema(description = "도서 인기 검색어 응답 DTO", hidden = true)
public class PopularSearchKeywordDto {

    // 최근 도서 인기 검색어 순위
    private final Integer rank;

    // 검색 화면에 표시할 정규화된 검색어
    private final String keyword;
}
