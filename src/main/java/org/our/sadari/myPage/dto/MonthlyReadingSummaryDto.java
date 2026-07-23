package org.our.sadari.myPage.dto;

import java.util.List;
import lombok.Data;
import org.our.sadari.report.dto.ReportDto;

/**
 * MonthlyReadingSummaryDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class MonthlyReadingSummaryDto {

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Long userNumb;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String periodStart;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String periodEndExclusive;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String reptStat;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String reportOrderType;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String monthCode;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String weekCode;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int currentWeekCount;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int previousWeekCount;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int weekCountDiff;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int currentMonthCount;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int previousMonthCount;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int countDiff;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String yearCode;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int currentYearCount;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int previousYearCount;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int yearCountDiff;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Integer monthGoalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Integer weekGoalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Integer yearGoalCnt;

    /**
     * ?대옒???대??먯꽌 ?ъ슜?섎뒗 ?곹깭 ?먮뒗 ?ㅼ젙 媛믪씠??
     */
    private Integer previousWeekGoalCnt;

    /**
     * ?대옒???대??먯꽌 ?ъ슜?섎뒗 ?곹깭 ?먮뒗 ?ㅼ젙 媛믪씠??
     */
    private Integer previousMonthGoalCnt;

    /**
     * ?대옒???대??먯꽌 ?ъ슜?섎뒗 ?곹깭 ?먮뒗 ?ㅼ젙 媛믪씠??
     */
    private Integer previousYearGoalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int monthGoalRate;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int weekGoalRate;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int yearGoalRate;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private boolean monthGoalSet;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private boolean weekGoalSet;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private boolean yearGoalSet;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int weekGoalRemainUpdateCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int monthGoalRemainUpdateCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int yearGoalRemainUpdateCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int weekGoalEditableRemainDays;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int monthGoalEditableRemainDays;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int yearGoalEditableRemainDays;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private boolean weekGoalUpdateLocked;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private boolean monthGoalUpdateLocked;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private boolean yearGoalUpdateLocked;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int weekGoalAchvCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int monthGoalAchvCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int yearGoalAchvCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int totalGoalAchvCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private List<ReportDto> currentMonthReports;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private List<ReportDto> currentWeekReports;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private List<ReportDto> currentYearReports;

    /**
     * 현재 읽고 있는 독후감 목록입니다.
     * 목표 종료일까지 남은 기간을 화면에서 계산할 수 있도록 시작일과 종료일을 함께 내려줍니다.
     */
    private List<ReportDto> currentReadingReports;
}
