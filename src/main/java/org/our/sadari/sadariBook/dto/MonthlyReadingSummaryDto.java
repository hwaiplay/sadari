package org.our.sadari.sadariBook.dto;

import java.util.List;
import lombok.Data;

/**
 * 마이페이지 독서 요약 조회 조건과 결과를 함께 담는 DTO입니다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class MonthlyReadingSummaryDto {

    /** 요약을 조회할 회원 번호입니다. */
    private Long userNumb;

    /** 조회 기간 시작일입니다. */
    private String periodStart;

    /** 조회 기간 종료 경계일입니다. SQL에서는 미만 조건으로 사용합니다. */
    private String periodEndExclusive;

    /** 월/연 단위 비교 기준일입니다. */
    private String targetDate;

    /** 화면 달력 아이콘에 표시할 월 코드입니다. */
    private String monthCode;

    /** 이번 달 완료 독서 권수입니다. */
    private int currentMonthCount;

    /** 지난 달 완료 독서 권수입니다. */
    private int previousMonthCount;

    /** 이번 달과 지난 달의 완료 독서 권수 차이입니다. */
    private int countDiff;

    /** 화면 달력 아이콘에 표시할 연도 코드입니다. */
    private String yearCode;

    /** 올해 완료 독서 권수입니다. */
    private int currentYearCount;

    /** 작년 완료 독서 권수입니다. */
    private int previousYearCount;

    /** 올해와 작년의 완료 독서 권수 차이입니다. */
    private int yearCountDiff;

    /** 이번 달 목표 독서 권수입니다. 목표를 설정하지 않았으면 null입니다. */
    private Integer monthGoalCnt;

    /** 올해 목표 독서 권수입니다. 목표를 설정하지 않았으면 null입니다. */
    private Integer yearGoalCnt;

    /** 이번 달 목표 달성률입니다. 목표를 설정하지 않았으면 0입니다. */
    private int monthGoalRate;

    /** 올해 목표 달성률입니다. 목표를 설정하지 않았으면 0입니다. */
    private int yearGoalRate;

    /** 이번 달 목표 설정 여부입니다. */
    private boolean monthGoalSet;

    /** 올해 목표 설정 여부입니다. */
    private boolean yearGoalSet;

    /** 이번 달 완료 독서 목록입니다. */
    private List<ReportDto> currentMonthReports;

    /** 올해 완료 독서 목록입니다. */
    private List<ReportDto> currentYearReports;
}
