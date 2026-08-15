package org.our.sadari.book.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.our.sadari.book.dto.PopularBookDto;
import org.our.sadari.book.mapper.BookMapper;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : BookPopularService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-16
 * description    : 주간과 월간 및 연간 독후감 작성자 수를 기준으로 인기 도서 목록을 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-16        SeungHyeon.Kang    최초 생성
 * 2026-08-16        SeungHyeon.Kang    주간과 월간 및 연간 집계 기간 선택 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookPopularService {

    // 서비스 인기 도서 집계에 사용하는 한국 표준시
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    // 현재 주 월요일부터 다음 주 월요일 전까지 집계하는 기간 코드
    private static final String PERIOD_WEEKLY = "weekly";
    // 현재 달 1일부터 다음 달 1일 전까지 집계하는 기간 코드
    private static final String PERIOD_MONTHLY = "monthly";
    // 현재 연도 1월 1일부터 다음 연도 1월 1일 전까지 집계하는 기간 코드
    private static final String PERIOD_YEARLY = "yearly";

    // 인기 도서 집계 데이터 접근 객체
    private final BookMapper bookMapper;

    /**
     * 선택한 현재 주와 달 또는 연도에 독후감을 작성한 고유 회원 수 기준 인기 도서를 최대 10권 조회한다
     *
     * @author SeungHyeon.Kang
     * @param period 주간과 월간 및 연간 중 조회할 집계 기간 코드
     * @return 순위와 독후감 작성자 수 및 평균 평점을 포함한 인기 도서 목록
     */
    public ResultData getPopularBookList(String period) {

        // 비어 있거나 지원하지 않는 기간은 임의의 넓은 집계 범위로 해석하지 않고 요청을 차단한다
        if (StringUtil.isEmpty(period)) {
            // "잘못된 요청입니다."
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        String normalizedPeriod = period.trim().toLowerCase(Locale.ROOT);
        LocalDate currentDate = LocalDate.now(SEOUL_ZONE);
        LocalDate periodStartDate;
        LocalDate nextPeriodStartDate;

        // 화면에서 선택한 집계 단위에 맞춰 현재 기간의 반개방 날짜 경계를 계산한다
        switch (normalizedPeriod) {
            // 주간 집계는 현재 날짜가 속한 주의 월요일부터 시작한다
            case PERIOD_WEEKLY:
                periodStartDate = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                // 다음 월요일 전까지를 현재 주간 집계 범위로 사용한다
                nextPeriodStartDate = periodStartDate.plusWeeks(1);
                break;
            // 월간 집계는 현재 날짜가 속한 달의 1일부터 시작한다
            case PERIOD_MONTHLY:
                periodStartDate = currentDate.withDayOfMonth(1);
                // 다음 달 1일 전까지를 현재 월간 집계 범위로 사용한다
                nextPeriodStartDate = periodStartDate.plusMonths(1);
                break;
            // 연간 집계는 현재 날짜가 속한 연도의 1월 1일부터 시작한다
            case PERIOD_YEARLY:
                periodStartDate = currentDate.withDayOfYear(1);
                // 다음 연도 1월 1일 전까지를 현재 연간 집계 범위로 사용한다
                nextPeriodStartDate = periodStartDate.plusYears(1);
                break;
            // 지원하지 않는 기간은 공통 잘못된 요청으로 처리한다
            default:
                // "잘못된 요청입니다."
                throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        LocalDateTime periodStart = periodStartDate.atStartOfDay();
        LocalDateTime nextPeriodStart = nextPeriodStartDate.atStartOfDay();
        // 회원 상태와 독서 상태 및 공개 여부를 제한하지 않은 기간별 인기 도서를 조회한다
        List<PopularBookDto> popularBookList = bookMapper.getPopularBookList(periodStart, nextPeriodStart);

        // 선택 기간에 독후감이 없으면 화면이 빈 인기 목록을 표시하도록 불변 빈 목록을 반환한다
        if (StringUtil.isEmpty(popularBookList)) {
            // 선택 기간의 인기 도서가 없는 성공 결과를 반환한다
            return ResultData.success(List.of());
        }

        // 정렬된 목록의 화면 순위를 1부터 차례로 설정한다
        for (int index = 0; index < popularBookList.size(); index++) {
            // 현재 목록 위치를 사용자가 확인할 1부터 시작하는 순위로 변환한다
            popularBookList.get(index).setRank(index + 1);
        }

        // 선택 기간의 인기 도서와 순위 및 독후감 작성자 수와 평균 평점을 반환한다
        return ResultData.success(popularBookList);
    }
}
