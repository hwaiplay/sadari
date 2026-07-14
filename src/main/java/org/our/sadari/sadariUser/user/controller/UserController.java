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
import org.our.sadari.global.common.util.CommonUtil;
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
 * 로그인 사용자의 프로필 정보와 마이페이지 데이터를 제공한다.
 * 프로필 수정 시에는 사용자 기본 정보와 프로필/배경 이미지 파일 번호를 함께 갱신한다.
 * @Author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final BookService bookService;
    private final FileService fileService;

    /**
     * 로그인 사용자의 현재 프로필 정보를 조회한다.
     * 화면에서 필요한 값만 Map으로 구성해 프로필 사진, 배경 사진, 닉네임, 한줄 소개를 반환한다.
     * @Author Seunghyeon.Kang
     * @param userNumb 인증 토큰에서 추출한 사용자 번호
     * @return 로그인 사용자 프로필 정보
     */
    @GetMapping("/me")
    public ResultData getMe(@AuthenticationPrincipal Long userNumb) {
        UserDto user = userMapper.getUserByNumb(userNumb);

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
     * 로그인 사용자의 프로필 정보를 수정한다.
     * 닉네임과 한줄 소개는 XSS 방어 처리를 거친 뒤 저장하고, 새 이미지가 전달된 경우 파일 테이블에 먼저 등록한 후 파일 번호를 사용자 테이블에 반영한다.
     * @Author Seunghyeon.Kang
     * @param userNumb 인증 토큰에서 추출한 사용자 번호
     * @param userDto 수정할 닉네임과 한줄 소개를 담은 사용자 정보
     * @param profileImage 새로 업로드한 프로필 이미지
     * @param backgroundImage 새로 업로드한 배경 이미지
     * @return 수정 후 다시 조회한 최신 프로필 정보
     * @throws IOException 이미지 파일 저장 중 오류가 발생한 경우
     */
    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ResultData uptMe(@AuthenticationPrincipal Long userNumb, @ModelAttribute UserDto userDto
                            , @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
                            , @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage) throws IOException {
        userDto.setUserNumb(userNumb);
        userDto.setUserNick(StringUtil.normalizePlainText(userDto.getUserNick(), 10));
        userDto.setIntrCntn(StringUtil.normalizePlainText(userDto.getIntrCntn(), 50));
        userDto.setProfNumb(fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, userNumb));
        userDto.setBgimNumb(fileService.setUploadedImage(backgroundImage, Constant.FILE_TYPE_BACKGROUND, userNumb));

        userMapper.uptUserProfile(userDto);
        return getMe(userNumb);
    }

    /**
     * 마이페이지에 표시할 이번 달 완료 독서 권수와 지난달 대비 변화량을 조회한다.
     * 독서 상태가 DONE이고 독서 종료일이 이미 도래한 독후감만 집계 대상에 포함한다.
     * @Author Seunghyeon.Kang
     * @param userNumb 인증 토큰에서 추출한 사용자 번호
     * @return 월간 완료 독서 요약 정보
     */
    @GetMapping("/monthly-reading-summary")
    public ResultData getMonthlyReadingSummary(@AuthenticationPrincipal Long userNumb) {
        MonthlyReadingSummaryDto summary = bookService.getMonthlyReadingSummary(userNumb);
        return ResultData.success(summary);
    }

    /**
     * 로그인 사용자의 독서 기록을 월간 달력 화면에 맞춰 조회한다.
     * 요청 월의 달력 표시 범위에 걸치는 독후감만 추려 날짜별 표시 데이터로 반환한다.
     * @Author Seunghyeon.Kang
     * @param userNumb 인증 토큰에서 추출한 사용자 번호
     * @param yearMonth 조회할 연월 값
     * @return 달력에 표시할 독후감 목록
     */
    @GetMapping("/reading-calendar")
    public ResultData getReadingCalendar(@AuthenticationPrincipal Long userNumb, @RequestParam("yearMonth") String yearMonth) {
        // 요청 월의 1일을 기준으로 달력 표시 시작일과 종료일을 계산한다.
        YearMonth targetMonth = YearMonth.parse(yearMonth);
        LocalDate monthStart = targetMonth.atDay(1);
        // 1일이 일요일로부터 며칠 떨어져 있는지 계산합니다. (일요일 시작 달력 기준, 전달의 잔여 일수 계산)
        // 월요일(1) ~ 토요일(6)은 그대로 유지되고, 일요일(7)은 0이 됩니다.
        int daysFromSunday = monthStart.getDayOfWeek().getValue() % 7;
        LocalDate calendarStart = monthStart.minusDays(daysFromSunday);
        // 주 7일 기준 총 6주(42일) 공간을 확보하기 위해 시작일로부터 41일을 더해 종료 날짜를 구합니다.
        LocalDate calendarEnd = calendarStart.plusDays(41);

        List<Map<String, Object>> calendarReports = new ArrayList<>();

        // 사용자의 전체 독후감 중 현재 달력 화면 범위와 겹치는 기록만 선별한다.
        for (ReportDto report : bookService.getBookList(userNumb, null, Constant.SORT_END_DATE_DESC)) {
            if (StringUtil.hasEmpty(report.getReportStdt(), report.getReportEndt())) {
                continue;
            }
            // 문자열 형태의 리포트 시작일과 종료일을 LocalDate 객체로 변환합니다.
            LocalDate reportStart = LocalDate.parse(report.getReportStdt());
            LocalDate reportEnd = LocalDate.parse(report.getReportEndt());

            // 독서 기간이 현재 달력에 표시되는 화면 범위(calendarStart ~ calendarEnd)와
            // 겹치지 않는 경우 달력 표시 대상에서 제외합니다.
            if (!CommonUtil.isDateRangeOverlapped(reportStart, reportEnd, calendarStart, calendarEnd)) {
                continue;
            }

            // 달력 화면에서 필요한 최소 필드만 반환해 프론트 표시 모델을 단순하게 유지한다.
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
