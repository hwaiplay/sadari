package org.our.sadari.myPage.controller;

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
    public ResultData getMonthlyReadingSummary(@AuthenticationPrincipal Long userNumb) {
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
    public ResultData setReadingGoal(@AuthenticationPrincipal Long userNumb, @RequestBody ReadingGoalDto readingGoalDto) {
        return reportService.setReadingGoal(userNumb, readingGoalDto);
    }

    /**
     * getReadingCalendar 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @return 처리 결과
     */
    @GetMapping("/reading-calendar")
    public ResultData getReadingCalendar(@AuthenticationPrincipal Long userNumb, @RequestParam("yearMonth") String yearMonth) {

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
