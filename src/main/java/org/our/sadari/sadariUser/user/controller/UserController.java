package org.our.sadari.sadariUser.user.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
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
import org.our.sadari.sadariBook.dto.MonthlyReadingSummaryDto;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.service.BookService;
import org.our.sadari.sadariUser.user.dto.UserDto;
import org.our.sadari.sadariUser.user.mapper.UserMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 로그인한 회원의 프로필, 마이페이지 요약, 독서 달력 API를 제공하는 컨트롤러입니다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final BookService bookService;
    private final FileService fileService;

    /**
     * 로그인한 회원의 프로필 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @return 프로필 정보를 담은 공통 응답
     */
    @GetMapping("/me")
    public ResultData getMe(@AuthenticationPrincipal Long userNumb) {
        UserDto user = userMapper.getUserByNumb(userNumb);

        // 회원 정보가 없으면 더 이상 프로필을 구성할 수 없으므로 빈 데이터 응답으로 처리합니다.
        if (StringUtil.isEmpty(user)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        Map<String, String> profile = new HashMap<>();
        profile.put("userNick", user.getUserNick());
        profile.put("porfPath", user.getPorfPath());
        profile.put("bgimPath", user.getBgimPath());
        profile.put("intrCntn", user.getIntrCntn());

        return ResultData.success(profile);
    }

    /**
     * 로그인한 회원의 프로필 사진, 배경사진, 닉네임, 한줄소개를 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param userDto 수정할 회원 기본 정보
     * @param profileImage 수정할 프로필 이미지 파일
     * @param backgroundImage 수정할 배경 이미지 파일
     * @return 수정 후 다시 조회한 프로필 정보
     * @throws IOException 이미지 파일 저장 중 오류가 발생한 경우
     */
    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ResultData uptMe(@AuthenticationPrincipal Long userNumb, @ModelAttribute UserDto userDto
                            , @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
                            , @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage) throws IOException {
        userDto.setUserNumb(userNumb);
        // 닉네임과 한줄소개는 DB 저장 전에 화면 정책 길이에 맞춰 공통 문자열 정리를 수행합니다.
        userDto.setUserNick(StringUtil.normalizePlainText(userDto.getUserNick(), 10));
        userDto.setIntrCntn(StringUtil.normalizePlainText(userDto.getIntrCntn(), 50));
        // 이미지가 전달되지 않으면 fileService가 null을 반환하고 Mapper의 동적 SQL이 기존 파일 번호를 유지합니다.
        userDto.setProfNumb(fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, userNumb));
        userDto.setBgimNumb(fileService.setUploadedImage(backgroundImage, Constant.FILE_TYPE_BACKGROUND, userNumb));

        userMapper.uptUserProfile(userDto);
        return getMe(userNumb);
    }

    /**
     * 마이페이지에 표시할 월간/연간 독서 요약을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @return 독서 요약 정보를 담은 공통 응답
     */
    @GetMapping("/monthly-reading-summary")
    public ResultData getMonthlyReadingSummary(@AuthenticationPrincipal Long userNumb) {
        MonthlyReadingSummaryDto summary = bookService.getMonthlyReadingSummary(userNumb);
        return ResultData.success(summary);
    }

    /**
     * 독서 달력 화면에 표시할 월간 독서 기록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param yearMonth 조회할 연월 값
     * @return 달력에 표시할 독서 기록 목록
     */
    @GetMapping("/reading-calendar")
    public ResultData getReadingCalendar(@AuthenticationPrincipal Long userNumb, @RequestParam("yearMonth") String yearMonth) {
        YearMonth targetMonth = YearMonth.parse(yearMonth);
        LocalDate monthStart = targetMonth.atDay(1);
        int daysFromSunday = monthStart.getDayOfWeek().getValue() % 7;
        LocalDate calendarStart = monthStart.minusDays(daysFromSunday);
        LocalDate calendarEnd = calendarStart.plusDays(41);

        List<Map<String, Object>> calendarReports = new ArrayList<>();

        // 달력은 앞뒤 월 날짜까지 포함해 6주를 보여주므로 현재 월과 겹치는 모든 독서 기간을 대상으로 합니다.
        for (ReportDto report : bookService.getBookList(userNumb, null, Constant.SORT_END_DATE_DESC)) {
            if (StringUtil.hasEmpty(report.getReportStdt(), report.getReportEndt())) {
                continue;
            }
            LocalDate reportStart = DateUtil.parseDefaultDate(report.getReportStdt());
            LocalDate reportEnd = DateUtil.parseDefaultDate(report.getReportEndt());

            // 독서 기간이 달력 표시 범위와 하루도 겹치지 않으면 화면에 표시하지 않습니다.
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
