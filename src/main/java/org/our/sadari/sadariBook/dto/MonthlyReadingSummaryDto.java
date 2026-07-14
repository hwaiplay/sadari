package org.our.sadari.sadariBook.dto;

import java.util.List;
import lombok.Data;

/**
 * 마이페이지 월간 완료 독서 통계 조회 조건과 응답 값을 담는다.
 * 이번 달과 지난달의 독서 완료 권수를 비교해 화면에서 월별 변화량을 표시할 때 사용한다.
 * @Author SeungHyeon.Kang
 */
@Data
public class MonthlyReadingSummaryDto {

    // 로그인 사용자의 독후감만 집계하기 위한 사용자 번호이다.
    private Long userNumb;
    // 집계 대상 월의 시작일이다.
    private String periodStart;
    // 집계 대상 다음 월의 시작일이며, SQL에서 미만 조건으로 사용한다.
    private String periodEndExclusive;
    // 집계 기준일이며, 종료일이 아직 오지 않은 독후감은 제외하기 위해 사용한다.
    private String targetDate;
    // 화면 달력 아이콘 안에 표시할 영문 월 약어이다.
    private String monthCode;
    // 이번 달 완료 독서 권수이다.
    private int currentMonthCount;
    // 지난달 완료 독서 권수이다.
    private int previousMonthCount;
    // 이번 달 완료 독서 권수에서 지난달 완료 독서 권수를 뺀 변화량이다.
    private int countDiff;
    // 화면 연간 달력 아이콘 안에 표시할 기준 연도이다.
    private String yearCode;
    // 올해 완료 독서 권수이다.
    private int currentYearCount;
    // 작년 완료 독서 권수이다.
    private int previousYearCount;
    // 올해 완료 독서 권수에서 작년 완료 독서 권수를 뺀 변화량이다.
    private int yearCountDiff;
    // 이번 달에 완료한 독후감 목록이다. 화면에서 요약 영역을 펼쳤을 때 바로 이동 가능한 항목으로 사용한다.
    private List<ReportDto> currentMonthReports;
    // 올해 완료한 독후감 목록이다. 월 목록과 동일한 응답에서 내려 화면의 추가 API 호출을 줄인다.
    private List<ReportDto> currentYearReports;
}
