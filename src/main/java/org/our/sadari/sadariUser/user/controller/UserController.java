package org.our.sadari.sadariUser.user.controller;

import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.CommonUtil;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.service.BookService;
import org.our.sadari.sadariUser.user.dto.UserDto;
import org.our.sadari.sadariUser.user.mapper.UserMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final BookService bookService;

    @GetMapping("/me")
    public ResultData getMe(@AuthenticationPrincipal Long userNumb) {
        UserDto user = userMapper.getUserByNumb(userNumb);

        if (StringUtil.isEmpty(user)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        Map<String, String> profile = new HashMap<>();
        profile.put("userNick", user.getUserNick());
        profile.put("porfPath", user.getPorfPath());

        return ResultData.success(profile);
    }

    @GetMapping("/reading-calendar")
    public ResultData getReadingCalendar(
            @AuthenticationPrincipal Long userNumb,
            @RequestParam("yearMonth") String yearMonth
    ) {
        YearMonth targetMonth = YearMonth.parse(yearMonth);
        LocalDate monthStart = targetMonth.atDay(1);
        int daysFromSunday = monthStart.getDayOfWeek().getValue() % 7;
        LocalDate calendarStart = monthStart.minusDays(daysFromSunday);
        LocalDate calendarEnd = calendarStart.plusDays(41);
        List<Map<String, Object>> calendarReports = new ArrayList<>();

        for (ReportDto report : bookService.getBookList(userNumb, null, Constant.SORT_END_DATE_DESC)) {
            if (StringUtil.hasEmpty(report.getReportStdt(), report.getReportEndt())) {
                continue;
            }

            LocalDate reportStart = LocalDate.parse(report.getReportStdt());
            LocalDate reportEnd = LocalDate.parse(report.getReportEndt());

            if (!CommonUtil.isDateRangeOverlapped(reportStart, reportEnd, calendarStart, calendarEnd)) {
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
