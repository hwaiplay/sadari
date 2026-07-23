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
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.report.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MyPageController 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "마이페이지", description = "독서 목표, 독서 요약, 독서 캘린더 API")
public class MyPageController {

    private final ReportService reportService;

    /**
     * getMonthlyReadingSummary 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @return 처리 결과
     */
    @GetMapping("/monthly-reading-summary")
    @Operation(summary = "독서 요약 조회", description = "로그인 사용자의 주간, 월간, 연간 독서 목표와 완료 독후감 요약을 조회한다.")
    public ResultData getMonthlyReadingSummary(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        return reportService.getMonthlyReadingSummary(userNumb);
    }

    /**
     * setReadingGoal 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @param readingGoalDto 처리에 필요한 입력값
     * @return 처리 결과
     */
    @PutMapping("/reading-goal")
    @Operation(summary = "독서 목표 저장", description = "로그인 사용자의 주간, 월간, 연간 독서 목표 권수를 저장한다.")
    public ResultData setReadingGoal(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb,
                                     @RequestBody ReadingGoalDto readingGoalDto) {
        return reportService.setReadingGoal(userNumb, readingGoalDto);
    }

    /**
     * 이전 목표량 복사
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @return 처리 결과
     */
    @PostMapping("/reading-goal/previous")
    @Operation(summary = "이전 독서 목표 복사", description = "현재 기간의 목표가 비어 있을 때 이전 주/월/년 목표 권수를 복사해 저장한다.")
    public ResultData copyPreviousReadingGoal(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        return reportService.copyPreviousReadingGoal(userNumb);
    }

    /**
     * getReadingCalendar 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @return 처리 결과
     */
    @GetMapping("/reading-calendar")
    @Operation(summary = "독서 캘린더 조회", description = "월 단위 캘린더에 표시할 독서 기간 데이터를 조회한다.")
    public ResultData getReadingCalendar(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb,
                                         @Parameter(description = "조회할 연월", example = "2026-07")
                                         @RequestParam("yearMonth") String yearMonth) {

        YearMonth targetMonth;

        try {
            targetMonth = YearMonth.parse(yearMonth);
        } catch (DateTimeParseException e) {
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 아래 처리 단계의 업무 목적을 설명한다.
        LocalDate monthStart = targetMonth.atDay(1);
        int daysFromSunday = monthStart.getDayOfWeek().getValue() % 7;
        LocalDate calendarStart = monthStart.minusDays(daysFromSunday);

        // 아래 처리 단계의 업무 목적을 설명한다.
        LocalDate calendarEnd = calendarStart.plusDays(41);
        List<Map<String, Object>> calendarReports = new ArrayList<>();
        ResultData bookListResult = reportService.getBookList(userNumb, null, Constant.SORT_END_DATE_DESC);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (bookListResult.getCode() != 200) {
            return bookListResult;
        }

        @SuppressWarnings("unchecked")
        List<ReportDto> bookList = (List<ReportDto>) bookListResult.getData();

        for (ReportDto report : bookList) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (StringUtil.hasEmpty(report.getReportStdt(), report.getReportEndt())) {
                continue;
            }

            LocalDate reportStart = DateUtil.parseDefaultDate(report.getReportStdt());
            LocalDate reportEnd = DateUtil.parseDefaultDate(report.getReportEndt());

            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (!DateUtil.isDateRangeOverlapped(reportStart, reportEnd, calendarStart, calendarEnd)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("reportNumb", report.getReportNumb());
            item.put("bookTitl", report.getBookTitl());
            item.put("reportStdt", report.getReportStdt());
            item.put("reportEndt", report.getReportEndt());
            item.put("reportColr", report.getReportColrName());
            calendarReports.add(item);
        }

        return ResultData.success(calendarReports);
    }
}
