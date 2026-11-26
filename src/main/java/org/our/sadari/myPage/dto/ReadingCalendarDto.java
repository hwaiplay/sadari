package org.our.sadari.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * fileName       : ReadingCalendarDto
 * author         : SeungHyeon.Kang
 * date           : 2026-11-26
 * description    : 독서 달력에 필요한 기간과 표시 정보만 전달
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-26        SeungHyeon.Kang    달력 전용 조회 응답 추가
 */
@Data
@Schema(description = "독서 달력 항목")
public class ReadingCalendarDto {

    @Schema(description = "독후감 번호", example = "3")
    private Long reptNumb;
    @Schema(description = "도서 제목", example = "데미안")
    private String bookTitl;
    @Schema(description = "독서 시작일", example = "2026-11-01")
    private String reptStdt;
    @Schema(description = "독서 종료일", example = "2026-11-25")
    private String reptEndt;
    @Schema(description = "달력 표시 색상", example = "#2f3437")
    private String reptColr;
}
