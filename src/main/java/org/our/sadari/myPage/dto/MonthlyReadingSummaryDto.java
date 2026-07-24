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
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
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
     * 마이페이지 프로필 통계에 표시할 전체 완료 독서 권수입니다.
     * 집계 SQL은 social 영역에서 수행하고, MyPageController가 기존 독서 요약 응답에 이 값을 합쳐 내려줍니다.
     */
    private int totalReadBookCnt;

    /**
     * 마이페이지 프로필 통계에 표시할 내가 팔로우하는 사용자 수입니다.
     * 팔로우 관계는 social 도메인의 데이터이므로 social 서비스 결과를 받아 세팅합니다.
     */
    private int followingCnt;

    /**
     * 마이페이지 프로필 통계에 표시할 나를 팔로우하는 사용자 수입니다.
     * 팔로워/팔로잉 기준을 화면에서 재계산하지 않도록 서버에서 확정된 값을 내려줍니다.
     */
    private int followerCnt;

    /**
     * 마이페이지 프로필 통계에 표시할 내 독후감이 받은 좋아요 수입니다.
     * TB_LIKEXX가 공용 좋아요 테이블이므로 TAGT_TYPE이 REPORT인 데이터만 social 쿼리에서 집계합니다.
     */
    private int receivedLikeCnt;

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
