package org.our.sadari.myPage.dto;

import lombok.Data;

/**
 * fileName       : ReadingStatisticsAggregateDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-15
 * description    : 독서 상태와 연속 기록 및 별점과 연도 비교 집계를 한 SQL 결과로 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-15        SeungHyeon.Kang    최초 생성
 */
@Data
public class ReadingStatisticsAggregateDto {

    // 읽는 중 독후감 수
    private long readCount;
    // 완독 독후감 수
    private long doneCount;
    // 중단 독후감 수
    private long stopCount;
    // 오늘 또는 어제까지 이어진 연속 독서일
    private int currentStreakDays;
    // 전체 기록 중 최장 연속 독서일
    private int longestStreakDays;
    // 0점대 독후감 수
    private long ratingZeroCount;
    // 1점대 독후감 수
    private long ratingOneCount;
    // 2점대 독후감 수
    private long ratingTwoCount;
    // 3점대 독후감 수
    private long ratingThreeCount;
    // 4점대 독후감 수
    private long ratingFourCount;
    // 5점 독후감 수
    private long ratingFiveCount;
    // 현재 연도 확정 독서 시간 초
    private long currentReadSecs;
    // 이전 연도 같은 기간 확정 독서 시간 초
    private long previousReadSecs;
    // 현재 연도 독서일 수
    private long currentReadDays;
    // 이전 연도 같은 기간 독서일 수
    private long previousReadDays;
    // 현재 연도 완독 권수
    private long currentDoneBooks;
    // 이전 연도 같은 기간 완독 권수
    private long previousDoneBooks;
}
