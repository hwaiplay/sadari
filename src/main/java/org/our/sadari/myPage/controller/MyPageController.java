package org.our.sadari.myPage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.DateUtil;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.myPage.dto.MonthlyReadingSummaryDto;
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.report.service.ReportService;
import org.our.sadari.social.dto.SocialDto;
import org.our.sadari.social.service.SocialService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : MyPageController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 마이페이지 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "마이페이지", description = "독서 목표, 독서 요약, 독서 캘린더 API")
public class MyPageController {

    // Report 업무 처리 서비스
    private final ReportService reportService;
    // Social 업무 처리 서비스
    private final SocialService socialService;

    /**
     * 로그인 사용자의 월간 독서 활동 요약 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리 대상 사용자 번호
     * @return 처리 결과
     */
    @GetMapping("/monthly-reading-summary")
    @Operation(summary = "독서 요약 조회", description = "로그인 사용자의 주간, 월간, 연간 독서 목표와 완료 독후감 요약을 조회한다.")
    public ResultData getMonthlyReadingSummary(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {

        // getMonthlyReadingSummary 업무 로직을 reportService에 위임한다
        ResultData summaryResult = reportService.getMonthlyReadingSummary(userNumb);

        // 독서 요약 조회가 실패하면 뒤의 통계 값을 붙이지 않고 원래 실패 응답을 그대로 내려준다.
        // 이렇게 해야 DB 오류나 인증 오류가 발생했을 때 화면이 일부 성공 데이터처럼 오해하지 않는다.
        if (summaryResult.getCode() != 200) {

            // 로그인 사용자의 월간 독서 활동 요약 조회 결과를 반환한다
            return summaryResult;
        }

        // getMyPageProfileStats 업무 로직을 socialService에 위임한다
        ResultData statsResult = socialService.getMyPageProfileStats(userNumb);

        // 마이페이지 API Controller는 응답 조합만 담당하고, 통계 집계 SQL과 기준은 social service/mapper에 둔다.
        // social 통계 조회가 실패하면 화면 통계만 비우지 않고 실패 사유를 그대로 반환해 공통 API 검증 흐름과 맞춘다.
        if (statsResult.getCode() != 200) {

            // 로그인 사용자의 월간 독서 활동 요약 조회 결과를 반환한다
            return statsResult;
        }

        // 공통 응답에 포함된 업무 데이터를 조회한다
        MonthlyReadingSummaryDto summary = (MonthlyReadingSummaryDto) summaryResult.getData();
        // 공통 응답에 포함된 업무 데이터를 조회한다
        SocialDto.ProfileStatsDto profileStats = (SocialDto.ProfileStatsDto) statsResult.getData();

        // profileStats 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (!StringUtil.isEmpty(profileStats)) {

            // TotalReadBookCnt 업무 값을 summary DTO에 설정한다
            summary.setTotalReadBookCnt(profileStats.getTotalReadBookCnt());
            // FollowingCnt 업무 값을 summary DTO에 설정한다
            summary.setFollowingCnt(profileStats.getFollowingCnt());
            // FollowerCnt 업무 값을 summary DTO에 설정한다
            summary.setFollowerCnt(profileStats.getFollowerCnt());
            // ReceivedLikeCnt 업무 값을 summary DTO에 설정한다
            summary.setReceivedLikeCnt(profileStats.getReceivedLikeCnt());
        }
        // 로그인 사용자의 월간 독서 활동 요약 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(summary);
    }

    /**
     * 로그인 사용자의 독서 목표 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리 대상 사용자 번호
     * @param readingGoalDto 저장할 독서 목표 기간과 권수
     * @return 처리 결과
     */
    @PutMapping("/reading-goal")
    @Operation(summary = "독서 목표 저장", description = "로그인 사용자의 주간, 월간, 연간 독서 목표 권수를 저장한다.")
    public ResultData setReadingGoal(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                   , @RequestBody ReadingGoalDto readingGoalDto) {

        // 로그인 사용자의 독서 목표 저장 결과를 반환한다
        return reportService.setReadingGoal(userNumb, readingGoalDto);
    }

    /**
     * 이전 목표량 복사
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리 대상 사용자 번호
     * @return 처리 결과
     */
    @PostMapping("/reading-goal/previous")
    @Operation(summary = "이전 독서 목표 복사", description = "현재 기간의 목표가 비어 있을 때 이전 주/월/년 목표 권수를 복사해 저장한다.")
    public ResultData copyPreviousReadingGoal(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {

        // 이전 목표량 복사 결과를 반환한다
        return reportService.copyPreviousReadingGoal(userNumb);
    }

    /**
     * 로그인 사용자의 독서 달력 데이터 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리 대상 사용자 번호
     * @return 처리 결과
     */
    @GetMapping("/reading-calendar")
    @Operation(summary = "독서 캘린더 조회", description = "월 단위 캘린더에 표시할 독서 기간 데이터를 조회한다.")
    public ResultData getReadingCalendar(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                       , @Parameter(description = "조회할 연월", example = "2026-07") @RequestParam("yearMonth") String yearMonth) {

        YearMonth targetMonth;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // parse 호출로 입력값을 필요한 데이터 형식으로 변환한다
            targetMonth = YearMonth.parse(yearMonth);
        }
        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (DateTimeParseException e) {

            // "\uC694\uCCAD\uAC12\uC774 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC544\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 아래 처리 단계의 업무 목적을 설명한다.
        LocalDate monthStart = targetMonth.atDay(1);
        // getDayOfWeek 조회로 후속 처리에 필요한 데이터를 가져온다
        int daysFromSunday = monthStart.getDayOfWeek().getValue() % 7;
        // 마이페이지 조회에 사용할 기준 날짜를 계산한다
        LocalDate calendarStart = monthStart.minusDays(daysFromSunday);

        // 아래 처리 단계의 업무 목적을 설명한다.
        LocalDate calendarEnd = calendarStart.plusDays(41);
        List<Map<String, Object>> calendarReports = new ArrayList<>();
        // getBookList 업무 로직을 reportService에 위임한다
        ResultData bookListResult = reportService.getBookList(userNumb, null, Constant.SORT_END_DATE_DESC);

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if (bookListResult.getCode() != 200) {

            // 로그인 사용자의 독서 달력 데이터 조회 결과를 반환한다
            return bookListResult;
        }

        @SuppressWarnings("unchecked")
        // 공통 응답에 포함된 업무 데이터를 조회한다
        List<ReportDto> bookList = (List<ReportDto>) bookListResult.getData();

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (ReportDto report : bookList) {

            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if (StringUtil.hasEmpty(report.getReptStdt(), report.getReptEndt())) {

                continue;
            }

            // 기본 날짜 형식의 문자열을 날짜 객체로 변환한다
            LocalDate reportStart = DateUtil.parseDefaultDate(report.getReptStdt());
            // 기본 날짜 형식의 문자열을 날짜 객체로 변환한다
            LocalDate reportEnd = DateUtil.parseDefaultDate(report.getReptEndt());

            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if (!DateUtil.isDateRangeOverlapped(reportStart, reportEnd, calendarStart, calendarEnd)) {

                continue;
            }

            Map<String, Object> item = new HashMap<>();
            // 후속 처리에 사용할 키와 값을 맵에 저장한다
            item.put("reptNumb", report.getReptNumb());
            // 후속 처리에 사용할 키와 값을 맵에 저장한다
            item.put("bookTitl", report.getBookTitl());
            // 후속 처리에 사용할 키와 값을 맵에 저장한다
            item.put("reptStdt", report.getReptStdt());
            // 후속 처리에 사용할 키와 값을 맵에 저장한다
            item.put("reptEndt", report.getReptEndt());
            // 후속 처리에 사용할 키와 값을 맵에 저장한다
            item.put("reptColr", report.getReptColrName());
            // 처리한 값을 결과 컬렉션에 추가한다
            calendarReports.add(item);
        }
        // 로그인 사용자의 독서 달력 데이터 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(calendarReports);
    }
}
