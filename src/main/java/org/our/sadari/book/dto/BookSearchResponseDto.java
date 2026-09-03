package org.our.sadari.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * fileName       : BookSearchResponseDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-16
 * description    : 도서 검색 페이지와 다음 조회 위치를 사용자 화면에 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-16        SeungHyeon.Kang    최초 생성
 */
@Data
@AllArgsConstructor
@Schema(description = "도서 검색 페이지 응답 DTO", hidden = true)
public class BookSearchResponseDto {

    @Schema(description = "현재 카카오 검색 페이지에서 조회된 최대 50권의 도서 목록")
    private List<BookJsonDto.BookDto> bookList;

    @Schema(description = "카카오 도서 검색의 마지막 페이지 여부")
    private boolean end;

    @Schema(description = "다음 카카오 검색 페이지에 대응하는 검색 결과 시작 위치")
    private Integer nextStart;
}
