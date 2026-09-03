package org.our.sadari.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;
import org.our.sadari.report.dto.ReportDto;

/**
 * fileName       : MonthlyReadingSummaryDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 마이페이지의 독서 활동과 목표 달성 요약을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Data
@Schema(description = "월간 독서 활동과 목표 달성 요약 DTO")
public class MonthlyReadingSummaryDto {

    @Schema(description = "현재 조회자에게 독서 목표를 공개하는지 여부", example = "Y", allowableValues = {"Y", "N"})
    private String goalPublicYsno;

    @Schema(description = "사용자 번호", example = "31")
    private Long userNumb;
    @Schema(description = "조회 기간 시작일", example = "2026-07-01")
    private String periodStart;
    @Schema(description = "조회 기간에 포함되지 않는 종료 기준일", example = "2026-08-01")
    private String periodEndExclusive;
    @Schema(description = "집계할 독서 상태 코드", example = "DONE")
    private String reptStat;
    @Schema(description = "독후감 목록 정렬 유형", example = "END_DATE_DESC")
    private String reportOrderType;
    @Schema(description = "월간 목표 유형 코드", example = "MONT")
    private String monthCode;
    @Schema(description = "주간 목표 유형 코드", example = "WEEK")
    private String weekCode;
    @Schema(description = "이번 주에 완료한 독서 권수", example = "2")
    private int currentWeekCount;
    @Schema(description = "지난 주에 완료한 독서 권수", example = "1")
    private int previousWeekCount;
    @Schema(description = "이번 주와 지난 주의 완료 독서 권수 차이", example = "1")
    private int weekCountDiff;
    @Schema(description = "이번 달에 완료한 독서 권수", example = "5")
    private int currentMonthCount;
    @Schema(description = "지난 달에 완료한 독서 권수", example = "4")
    private int previousMonthCount;
    @Schema(description = "이번 달과 지난 달의 완료 독서 권수 차이", example = "1")
    private int countDiff;
    @Schema(description = "연간 목표 유형 코드", example = "YEAR")
    private String yearCode;
    @Schema(description = "올해 완료한 독서 권수", example = "30")
    private int currentYearCount;
    @Schema(description = "지난해 완료한 독서 권수", example = "24")
    private int previousYearCount;
    @Schema(description = "올해와 지난해의 완료 독서 권수 차이", example = "6")
    private int yearCountDiff;
    @Schema(description = "이번 달 목표 권수", example = "5")
    private Integer monthGoalCnt;
    @Schema(description = "이번 주 목표 권수", example = "2")
    private Integer weekGoalCnt;
    @Schema(description = "올해 목표 권수", example = "60")
    private Integer yearGoalCnt;
    @Schema(description = "지난 주 목표 권수", example = "2")
    private Integer previousWeekGoalCnt;
    @Schema(description = "지난 달 목표 권수", example = "5")
    private Integer previousMonthGoalCnt;
    @Schema(description = "지난해 목표 권수", example = "50")
    private Integer previousYearGoalCnt;
    @Schema(description = "월간 목표 달성률", example = "100")
    private int monthGoalRate;
    @Schema(description = "주간 목표 달성률", example = "50")
    private int weekGoalRate;
    @Schema(description = "연간 목표 달성률", example = "50")
    private int yearGoalRate;
    @Schema(description = "월간 목표 설정 여부", example = "true")
    private boolean monthGoalSet;
    @Schema(description = "주간 목표 설정 여부", example = "true")
    private boolean weekGoalSet;
    @Schema(description = "연간 목표 설정 여부", example = "true")
    private boolean yearGoalSet;
    @Schema(description = "주간 목표를 추가로 수정할 수 있는 횟수", example = "1")
    private int weekGoalRemainUpdateCnt;
    @Schema(description = "월간 목표를 추가로 수정할 수 있는 횟수", example = "1")
    private int monthGoalRemainUpdateCnt;
    @Schema(description = "연간 목표를 추가로 수정할 수 있는 횟수", example = "1")
    private int yearGoalRemainUpdateCnt;
    @Schema(description = "주간 목표 수정 가능 기간의 남은 일수", example = "3")
    private int weekGoalEditableRemainDays;
    @Schema(description = "월간 목표 수정 가능 기간의 남은 일수", example = "10")
    private int monthGoalEditableRemainDays;
    @Schema(description = "연간 목표 수정 가능 기간의 남은 일수", example = "120")
    private int yearGoalEditableRemainDays;
    @Schema(description = "주간 목표 수정 잠금 여부", example = "false")
    private boolean weekGoalUpdateLocked;
    @Schema(description = "월간 목표 수정 잠금 여부", example = "false")
    private boolean monthGoalUpdateLocked;
    @Schema(description = "연간 목표 수정 잠금 여부", example = "false")
    private boolean yearGoalUpdateLocked;
    @Schema(description = "주간 목표 달성 횟수", example = "3")
    private int weekGoalAchvCnt;
    @Schema(description = "월간 목표 달성 횟수", example = "2")
    private int monthGoalAchvCnt;
    @Schema(description = "연간 목표 달성 횟수", example = "1")
    private int yearGoalAchvCnt;
    @Schema(description = "전체 독서 목표 달성 횟수", example = "6")
    private int totalGoalAchvCnt;
    @Schema(description = "완료 상태인 전체 독서 권수", example = "30")
    private int totalReadBookCnt;
    @Schema(description = "사용자가 팔로우하는 계정 수", example = "8")
    private int followingCnt;
    @Schema(description = "사용자를 팔로우하는 계정 수", example = "5")
    private int followerCnt;
    @Schema(description = "사용자의 독후감이 받은 좋아요 수", example = "42")
    private int receivedLikeCnt;
    @Schema(description = "이번 달에 완료한 독후감 목록")
    private List<ReportDto> currentMonthReports;
    @Schema(description = "이번 주에 완료한 독후감 목록")
    private List<ReportDto> currentWeekReports;
    @Schema(description = "올해 완료한 독후감 목록")
    private List<ReportDto> currentYearReports;
    @Schema(description = "현재 읽고 있는 독후감 목록")
    private List<ReportDto> currentReadingReports;
}
