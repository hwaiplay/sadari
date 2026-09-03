package org.our.sadari.myPage.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : ReadingStatisticsQueryDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 통계 집계 SQL에 필요한 회원과 현재 및 비교 기간 조건을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-15        SeungHyeon.Kang    통합 연속 기록 집계 기준일 추가
 */
@Data
public class ReadingStatisticsQueryDto {

    // 통계를 조회할 회원 번호
    private Long userNumb;
    // 현재 연도
    private int currentYear;
    // 비교할 이전 연도
    private int previousYear;
    // 현재 연도 일별 집계 시작일
    private LocalDate currentStart;
    // 현재 연도 일별 집계 종료 제외일
    private LocalDate currentEnd;
    // 이전 연도 일별 집계 시작일
    private LocalDate previousStart;
    // 이전 연도 일별 집계 종료 제외일
    private LocalDate previousEnd;
    // 현재 연도 타이머 상세 조회 시작 일시
    private LocalDateTime timerStart;
    // 현재 연도 타이머 상세 조회 종료 제외 일시
    private LocalDateTime timerEnd;
    // 완료 타이머 상태 코드
    private String completedStat;
    // 완독 독후감 상태 코드
    private String doneStat;
    // 연속 독서 기록 계산 기준일
    private LocalDate today;
}
