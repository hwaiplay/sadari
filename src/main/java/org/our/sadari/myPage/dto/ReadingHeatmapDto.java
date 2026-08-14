package org.our.sadari.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * fileName       : ReadingHeatmapDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 선택 연도의 독서 시간 잔디 데이터만 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "선택 연도 독서 시간 잔디 DTO")
public class ReadingHeatmapDto {

    @Schema(description = "독서 시간 잔디 시작일", example = "2026-01-01")
    private LocalDate heatmapStart;
    @Schema(description = "독서 시간 잔디 종료일", example = "2026-08-14")
    private LocalDate heatmapEnd;
    @Schema(description = "선택 연도의 날짜별 독서 시간 목록")
    private List<ReadingStatisticsDto.Daily> heatmapList;
    @Schema(description = "잔디에 표시한 연도", example = "2026")
    private int selectedYear;
    @Schema(description = "잔디로 조회할 수 있는 연도 목록", example = "[2026, 2025]")
    private List<Integer> availableYears;
}
