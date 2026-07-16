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

    /** 화면 달력 아이콘에 표시할 월 코드입니다. */
    private String monthCode;

    /** 이번 주를 화면에 표시할 주차 코드입니다. */
    private String weekCode;

    /** 이번 주 완료 독서 권수입니다. */
    private int currentWeekCount;

    /** 지난주 완료 독서 권수입니다. */
    private int previousWeekCount;

    /** 이번 주와 지난주의 완료 독서 권수 차이입니다. */
    private int weekCountDiff;

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

    /** 이번 주 목표 독서 권수입니다. 목표를 설정하지 않았으면 null입니다. */
    private Integer weekGoalCnt;

    /** 올해 목표 독서 권수입니다. 목표를 설정하지 않았으면 null입니다. */
    private Integer yearGoalCnt;

    /** 이번 달 목표 달성률입니다. 목표를 설정하지 않았으면 0입니다. */
    private int monthGoalRate;

    /** 이번 주 목표 달성률입니다. 목표를 설정하지 않았으면 0입니다. */
    private int weekGoalRate;

    /** 올해 목표 달성률입니다. 목표를 설정하지 않았으면 0입니다. */
    private int yearGoalRate;

    /** 이번 달 목표 설정 여부입니다. */
    private boolean monthGoalSet;

    /** 이번 주 목표 설정 여부입니다. */
    private boolean weekGoalSet;

    /** 올해 목표 설정 여부입니다. */
    private boolean yearGoalSet;

    /** 주간 목표를 이번 기간 안에서 앞으로 몇 번 더 수정할 수 있는지 표시합니다. */
    private int weekGoalRemainUpdateCnt;

    /** 월간 목표를 이번 기간 안에서 앞으로 몇 번 더 수정할 수 있는지 표시합니다. */
    private int monthGoalRemainUpdateCnt;

    /** 연간 목표를 이번 기간 안에서 앞으로 몇 번 더 수정할 수 있는지 표시합니다. */
    private int yearGoalRemainUpdateCnt;

    /** 주간 목표 수정 가능 기간이 잠기기 전까지 남은 일수입니다. 0이면 오늘은 수정할 수 없습니다. */
    private int weekGoalEditableRemainDays;

    /** 월간 목표 수정 가능 기간이 잠기기 전까지 남은 일수입니다. 0이면 오늘은 수정할 수 없습니다. */
    private int monthGoalEditableRemainDays;

    /** 연간 목표 수정 가능 기간이 잠기기 전까지 남은 일수입니다. 0이면 오늘은 수정할 수 없습니다. */
    private int yearGoalEditableRemainDays;

    /** 주간 목표가 기간 제한 때문에 현재 수정 불가능한 상태인지 표시합니다. */
    private boolean weekGoalUpdateLocked;

    /** 월간 목표가 기간 제한 때문에 현재 수정 불가능한 상태인지 표시합니다. */
    private boolean monthGoalUpdateLocked;

    /** 연간 목표가 기간 제한 때문에 현재 수정 불가능한 상태인지 표시합니다. */
    private boolean yearGoalUpdateLocked;

    /** 주간 목표를 달성한 횟수입니다. */
    private int weekGoalAchvCnt;

    /** 월간 목표를 달성한 횟수입니다. */
    private int monthGoalAchvCnt;

    /** 연간 목표를 달성한 횟수입니다. */
    private int yearGoalAchvCnt;

    /** 주간, 월간, 연간 목표를 모두 합산한 총 달성 횟수입니다. */
    private int totalGoalAchvCnt;

    /** 이번 달 완료 독서 목록입니다. */
    private List<ReportDto> currentMonthReports;

    /** 이번 주 완료 독서 목록입니다. */
    private List<ReportDto> currentWeekReports;

    /** 올해 완료 독서 목록입니다. */
    private List<ReportDto> currentYearReports;
}
