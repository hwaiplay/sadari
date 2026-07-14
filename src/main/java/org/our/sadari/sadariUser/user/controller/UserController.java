package org.our.sadari.sadariUser.user.controller;

import lombok.RequiredArgsConstructor;
import java.io.IOException;
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
import org.our.sadari.global.common.util.XssUtil;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final BookService bookService;
    private final FileService fileService;

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
     * 로그인 사용자의 프로필 사진, 닉네임, 한줄 소개를 수정한다.
     * @Author Seunghyeon.Kang
     * @param userNumb 인증된 사용자 번호
     * @param userDto 수정할 닉네임과 한줄 소개
     * @param profileImage 새로 업로드한 프로필 이미지
     * @return 수정 후 프로필 정보
     * @throws IOException 프로필 이미지 저장 실패 시 발생
     */
    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ResultData uptMe(@AuthenticationPrincipal Long userNumb, @ModelAttribute UserDto userDto
                            , @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
                            , @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage) throws IOException {
        userDto.setUserNumb(userNumb);
        userDto.setUserNick(XssUtil.escape(StringUtil.cutString(userDto.getUserNick(), 50)));
        userDto.setIntrCntn(XssUtil.escape(StringUtil.cutString(userDto.getIntrCntn(), 100)));
        userDto.setProfNumb(fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, userNumb));
        userDto.setBgimNumb(fileService.setUploadedImage(backgroundImage, Constant.FILE_TYPE_BACKGROUND, userNumb));

        userMapper.uptUserProfile(userDto);
        return getMe(userNumb);
    }

    @GetMapping("/reading-calendar")
    public ResultData getReadingCalendar(@AuthenticationPrincipal Long userNumb,@RequestParam("yearMonth") String yearMonth) {
        // 1. 달력 화면을 구성하기 위한 기간(시작일, 종료일) 계산
        // 전달받은 문자열을 YearMonth 객체로 파싱합니다.
        YearMonth targetMonth = YearMonth.parse(yearMonth);
        // 해당 월의 1일을 구합니다.
        LocalDate monthStart = targetMonth.atDay(1);
        // 1일이 일요일로부터 며칠 떨어져 있는지 계산합니다. (일요일 시작 달력 기준, 전달의 잔여 일수 계산)
        // 월요일(1) ~ 토요일(6)은 그대로 유지되고, 일요일(7)은 0이 됩니다.
        int daysFromSunday = monthStart.getDayOfWeek().getValue() % 7;
        // 달력 첫 화면에 표시될 시작 날짜를 구합니다. (1일이 속한 주의 일요일 날짜)
        LocalDate calendarStart = monthStart.minusDays(daysFromSunday);
        // 주 7일 기준 총 6주(42일) 공간을 확보하기 위해 시작일로부터 41일을 더해 종료 날짜를 구합니다.
        LocalDate calendarEnd = calendarStart.plusDays(41);
        // 결과를 담을 캘린더 리포트 목록 객체를 생성합니다.
        List<Map<String, Object>> calendarReports = new ArrayList<>();
        // 2. 사용자의 전체 독서 리포트 목록을 조회하여 달력 기간에 포함되는지 필터링
        // 종료일 내림차순으로 사용자의 전체 독서 리포트 목록을 가져옵니다.
        for (ReportDto report : bookService.getBookList(userNumb, null, Constant.SORT_END_DATE_DESC)) {

            // 시작일이나 종료일 정보가 없는 불완전한 데이터는 제외하고 다음으로 넘어갑니다.
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
            // 3. 달력 화면 표시용 데이터 매핑
            // 달력 노출에 필요한 데이터만 추출하여 맵 구조에 담습니다.
            Map<String, Object> item = new HashMap<>();
            item.put("reportNumb", report.getReportNumb());
            item.put("bookTitl", report.getBookTitl());
            item.put("reportStdt", report.getReportStdt());
            item.put("reportEndt", report.getReportEndt());
            item.put("reportColr", report.getReportColrName()); // 리포트 배경색 등 시각 요소

            // 정제된 데이터를 결과 리스트에 추가합니다.
            calendarReports.add(item);
        }

        // 4. 성공 응답 반환
        // 캘린더 데이터를 규격화된 성공 응답 객체에 담아 반환합니다.
        return ResultData.success(calendarReports);
    }
}
