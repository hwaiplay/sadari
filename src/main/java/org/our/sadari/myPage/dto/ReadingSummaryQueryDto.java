package org.our.sadari.myPage.dto;

import lombok.Data;

/**
 * fileName       : ReadingSummaryQueryDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 독서 요약 통합 조회의 기간 조건과 집계 결과를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 * 2026-08-04        OpenAI.Codex       선택적 독후감 공개 범위 조건 추가
 */
@Data
public class ReadingSummaryQueryDto {

    // 조회할 사용자 번호
    private Long userNumb;

    // 완료 독서 상태 코드
    private String doneStat;

    // 읽는 중 독서 상태 코드
    private String readStat;

    // 다른 사용자 화면에 적용할 독후감 공개 여부
    private String pubcYsno;

    // 주간 목표 유형 코드
    private String weekGoalType;

    // 월간 목표 유형 코드
    private String monthGoalType;

    // 연간 목표 유형 코드
    private String yearGoalType;

    // 현재 주 시작일
    private String currentWeekStart;

    // 다음 주 시작일
    private String nextWeekStart;

    // 이전 주 시작일
    private String previousWeekStart;

    // 현재 월 시작일
    private String currentMonthStart;

    // 다음 월 시작일
    private String nextMonthStart;

    // 이전 월 시작일
    private String previousMonthStart;

    // 현재 연도 시작일
    private String currentYearStart;

    // 이전 연도 시작일
    private String previousYearStart;

    // 다음 연도 시작일
    private String nextYearStart;

    // 현재 주 목표 기준값
    private String currentWeekGoalDate;

    // 이전 주 목표 기준값
    private String previousWeekGoalDate;

    // 현재 월 목표 기준값
    private String currentMonthGoalDate;

    // 이전 월 목표 기준값
    private String previousMonthGoalDate;

    // 현재 연도 목표 기준값
    private String currentYearGoalDate;

    // 이전 연도 목표 기준값
    private String previousYearGoalDate;

    // 현재 주 완료 독서 권수
    private int currentWeekCount;

    // 이전 주 완료 독서 권수
    private int previousWeekCount;

    // 현재 월 완료 독서 권수
    private int currentMonthCount;

    // 이전 월 완료 독서 권수
    private int previousMonthCount;

    // 현재 연도 완료 독서 권수
    private int currentYearCount;

    // 이전 연도 완료 독서 권수
    private int previousYearCount;

    // 현재 주 목표 권수
    private Integer weekGoalCnt;

    // 이전 주 목표 권수
    private Integer previousWeekGoalCnt;

    // 현재 월 목표 권수
    private Integer monthGoalCnt;

    // 이전 월 목표 권수
    private Integer previousMonthGoalCnt;

    // 현재 연도 목표 권수
    private Integer yearGoalCnt;

    // 이전 연도 목표 권수
    private Integer previousYearGoalCnt;

    // 현재 주 목표 수정 횟수
    private Integer weekGoalUpdtCnt;

    // 현재 월 목표 수정 횟수
    private Integer monthGoalUpdtCnt;

    // 현재 연도 목표 수정 횟수
    private Integer yearGoalUpdtCnt;

    // 주간 목표 달성 횟수
    private int weekGoalAchvCnt;

    // 월간 목표 달성 횟수
    private int monthGoalAchvCnt;

    // 연간 목표 달성 횟수
    private int yearGoalAchvCnt;
}
