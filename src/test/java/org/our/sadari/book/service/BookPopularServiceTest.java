package org.our.sadari.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.book.dto.PopularBookDto;
import org.our.sadari.book.mapper.BookMapper;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;

/**
 * fileName       : BookPopularServiceTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-16
 * description    : 주간과 월간 및 연간 인기 도서의 조회 기간과 화면 순위를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-16        SeungHyeon.Kang    최초 생성 및 기간별 집계 검증
 */
@ExtendWith(MockitoExtension.class)
class BookPopularServiceTest {

    // 인기 도서 집계 데이터 접근 Mock
    @Mock
    private BookMapper bookMapper;
    // 기간별 인기 도서 서비스 단위 테스트 대상
    private BookPopularService bookPopularService;

    /**
     * 각 테스트에 독립된 기간별 인기 도서 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // Mock Mapper를 사용하는 기간별 인기 도서 서비스를 생성한다
        bookPopularService = new BookPopularService(bookMapper);
    }

    /**
     * 현재 달의 반개방 기간으로 조회하고 정렬된 결과에 1부터 순위를 부여한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getMonthlyPopularBookListSetsSequentialRank() {
        // Mapper가 반환할 첫 번째 인기 도서를 생성한다
        PopularBookDto firstBook = new PopularBookDto();
        // 첫 번째 도서의 고유 독후감 작성자 수를 설정한다
        firstBook.setReportCount(8L);
        // Mapper가 반환할 두 번째 인기 도서를 생성한다
        PopularBookDto secondBook = new PopularBookDto();
        // 두 번째 도서의 고유 독후감 작성자 수를 설정한다
        secondBook.setReportCount(5L);
        // Mapper 정렬 결과를 그대로 서비스에 반환하도록 설정한다
        List<PopularBookDto> popularBookList = List.of(firstBook, secondBook);
        // 현재 달의 어떤 시작 및 종료 경계에도 준비한 인기 도서를 반환한다
        when(bookMapper.getPopularBookList(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(popularBookList);

        // 이번 달 인기 도서 목록을 조회한다
        ResultData result = bookPopularService.getPopularBookList("monthly");

        // 인기 도서 조회 성공 코드를 확인한다
        assertEquals(200, result.getCode());
        // Mapper가 반환한 목록 객체가 화면 응답에 사용되는지 확인한다
        assertSame(popularBookList, result.getData());
        // 첫 번째 인기 도서에 1위가 설정되는지 확인한다
        assertEquals(1, firstBook.getRank());
        // 두 번째 인기 도서에 2위가 설정되는지 확인한다
        assertEquals(2, secondBook.getRank());
        // 현재 달 시작 경계를 확인할 인자 캡처 객체를 생성한다
        ArgumentCaptor<LocalDateTime> monthStartCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        // 다음 달 시작 경계를 확인할 인자 캡처 객체를 생성한다
        ArgumentCaptor<LocalDateTime> nextMonthStartCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        // Mapper에 전달된 월간 집계 시작과 종료 경계를 캡처한다
        verify(bookMapper).getPopularBookList(monthStartCaptor.capture(), nextMonthStartCaptor.capture());
        // 현재 달 시작 경계가 1일로 계산되는지 확인한다
        assertEquals(1, monthStartCaptor.getValue().getDayOfMonth());
        // 현재 달 시작 경계가 자정으로 계산되는지 확인한다
        assertEquals(0, monthStartCaptor.getValue().getHour());
        // 다음 달 시작 경계가 현재 달 시작의 정확히 한 달 뒤인지 확인한다
        assertEquals(monthStartCaptor.getValue().plusMonths(1), nextMonthStartCaptor.getValue());
    }

    /**
     * 이번 달 독후감이 없으면 빈 인기 도서 목록을 반환한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getMonthlyPopularBookListReturnsEmptyList() {
        // 이번 달 집계 대상이 없는 Mapper 결과를 설정한다
        when(bookMapper.getPopularBookList(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // 이번 달 인기 도서 목록을 조회한다
        ResultData result = bookPopularService.getPopularBookList("monthly");

        // 빈 목록도 정상 조회 성공으로 처리되는지 확인한다
        assertEquals(200, result.getCode());
        // 이번 달 인기 도서가 없으면 빈 목록이 반환되는지 확인한다
        assertEquals(List.of(), result.getData());
    }

    /**
     * 주간 인기 도서는 서울 시간 기준 월요일부터 다음 월요일 전까지 조회한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getWeeklyPopularBookListUsesMondayBoundary() {
        // 주간 집계 기간 경계를 확인할 수 있도록 빈 Mapper 결과를 설정한다
        when(bookMapper.getPopularBookList(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // 현재 주간 인기 도서 목록을 조회한다
        bookPopularService.getPopularBookList("weekly");

        // 주간 시작 경계를 확인할 인자 캡처 객체를 생성한다
        ArgumentCaptor<LocalDateTime> periodStartCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        // 다음 주 시작 경계를 확인할 인자 캡처 객체를 생성한다
        ArgumentCaptor<LocalDateTime> nextPeriodStartCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        // Mapper에 전달된 주간 집계 시작과 종료 경계를 캡처한다
        verify(bookMapper).getPopularBookList(periodStartCaptor.capture(), nextPeriodStartCaptor.capture());
        // 주간 집계가 월요일에 시작하는지 확인한다
        assertEquals(DayOfWeek.MONDAY, periodStartCaptor.getValue().getDayOfWeek());
        // 다음 집계 경계가 정확히 일주일 뒤인지 확인한다
        assertEquals(periodStartCaptor.getValue().plusWeeks(1), nextPeriodStartCaptor.getValue());
    }

    /**
     * 연간 인기 도서는 서울 시간 기준 1월 1일부터 다음 연도 1월 1일 전까지 조회한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getYearlyPopularBookListUsesYearBoundary() {
        // 연간 집계 기간 경계를 확인할 수 있도록 빈 Mapper 결과를 설정한다
        when(bookMapper.getPopularBookList(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // 현재 연간 인기 도서 목록을 조회한다
        bookPopularService.getPopularBookList("yearly");

        // 연간 시작 경계를 확인할 인자 캡처 객체를 생성한다
        ArgumentCaptor<LocalDateTime> periodStartCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        // 다음 연도 시작 경계를 확인할 인자 캡처 객체를 생성한다
        ArgumentCaptor<LocalDateTime> nextPeriodStartCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        // Mapper에 전달된 연간 집계 시작과 종료 경계를 캡처한다
        verify(bookMapper).getPopularBookList(periodStartCaptor.capture(), nextPeriodStartCaptor.capture());
        // 연간 집계가 1월 1일에 시작하는지 확인한다
        assertEquals(1, periodStartCaptor.getValue().getDayOfYear());
        // 다음 집계 경계가 정확히 일 년 뒤인지 확인한다
        assertEquals(periodStartCaptor.getValue().plusYears(1), nextPeriodStartCaptor.getValue());
    }

    /**
     * 지원하지 않는 집계 기간은 Mapper를 호출하지 않고 잘못된 요청으로 처리한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getPopularBookListRejectsInvalidPeriod() {
        // 지원하지 않는 기간으로 인기 도서를 조회할 때 발생하는 업무 예외를 확인한다
        CustomException exception = assertThrows(CustomException.class
                                                , () -> bookPopularService.getPopularBookList("daily"));

        // 잘못된 기간이 공통 잘못된 요청 코드로 처리되는지 확인한다
        assertEquals(ResultEnum.COMMON_INVALID_REQUEST, exception.getResultEnum());
        // 유효하지 않은 기간은 DB 조회를 실행하지 않는지 확인한다
        verifyNoInteractions(bookMapper);
    }
}
