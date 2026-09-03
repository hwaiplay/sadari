package org.our.sadari.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * fileName       : BookCoverColorRequestDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 도서 표지 대표색 분석 요청 데이터를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    카카오 도서 표지 URL 계약 반영
 */
@Data
@NoArgsConstructor
@Schema(description = "도서 표지 대표색 분석 요청 DTO")
public class BookCoverColorRequestDto {

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "신뢰된 도서 검색 공급자의 표지 이미지 URL", example = "https://search1.kakaocdn.net/thumb/R120x174.q85/book-cover")
    private String bookCvim;
}
