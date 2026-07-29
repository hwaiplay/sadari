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
 * description    : 도서 표지 대표색 분석 요청 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Data
@NoArgsConstructor
@Schema(description = "도서 표지 대표색 분석 요청 DTO")
public class BookCoverColorRequestDto {

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "네이버 도서 표지 이미지 URL", example = "https://shopping-phinf.pstatic.net/main_1234567/123456789.jpg")
    private String bookCvim;
}
