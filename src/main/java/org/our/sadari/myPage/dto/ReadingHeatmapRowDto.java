package org.our.sadari.myPage.dto;

import java.time.LocalDate;
import lombok.Data;

/**
 * fileName       : ReadingHeatmapRowDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-15
 * description    : 조회 가능한 독서 연도와 선택 연도의 일별 독서 시간을 한 SQL 결과로 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-15        SeungHyeon.Kang    최초 생성
 */
@Data
public class ReadingHeatmapRowDto {

    // 조회 가능 연도를 나타내는 행 유형
    public static final String ROW_TYPE_YEAR = "YEAR";
    // 선택 연도의 일별 독서 시간을 나타내는 행 유형
    public static final String ROW_TYPE_DAILY = "DAILY";

    // 연도 또는 일별 시간 행 구분값
    private String rowType;
    // 독서 기록이 존재하는 연도
    private Integer readYear;
    // 선택 연도의 독서 날짜
    private LocalDate readDate;
    // 선택 날짜의 확정 독서 시간 초
    private long readSecs;
}
