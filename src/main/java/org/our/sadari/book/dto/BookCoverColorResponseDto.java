package org.our.sadari.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * fileName       : BookCoverColorResponseDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 도서 표지와 가장 가까운 책장 색상 코드 응답을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Getter
@AllArgsConstructor
@Schema(description = "도서 표지 대표색 기반 책장 색상 응답 DTO")
public class BookCoverColorResponseDto {

    @Schema(description = "BOOK_COLR 공통코드에서 선택된 세부코드", example = "CORAL_RED")
    private final String reptColr;

    @Schema(description = "선택된 세부코드의 HEX 색상", example = "#c96f64")
    private final String reptColrName;
}
