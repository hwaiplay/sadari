package org.our.sadari.myPage.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.myPage.dto.MonthlyReadingSummaryDto;
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.myPage.service.ReadingStatisticsService;
import org.our.sadari.report.service.ReportService;
import org.our.sadari.social.dto.SocialDto;
import org.our.sadari.social.service.SocialService;

/**
 * fileName       : MyPageControllerTest
 * author         : SeungHyeon.Kang
 * date           : 2026-10-12
 * description    : 목표 저장·복사 응답의 요약 재사용과 실패 전달 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-10-12        SeungHyeon.Kang        최초 생성
 */
@ExtendWith(MockitoExtension.class)
class MyPageControllerTest {

    // 목표 저장과 요약 조회 서비스 대역
    @Mock
    private ReportService reportService;
    // 소셜 통계 서비스 대역
    @Mock
    private SocialService socialService;
    // 컨트롤러의 독서 통계 의존성 대역
    @Mock
    private ReadingStatisticsService readingStatisticsService;
    // 실제 목표 저장·복사 응답 조합 대상
    @InjectMocks
    private MyPageController controller;

    /**
     * 목표 저장·복사가 조회한 요약을 재사용하면서 소셜 통계를 유지하는지 검증
     *
     * @author SeungHyeon.Kang
     * @param copy 이전 목표 복사 여부
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void goalSummaryIsReused(boolean copy) {

        // 서비스가 저장 후 이미 조회한 독서 요약 생성
        MonthlyReadingSummaryDto summary = new MonthlyReadingSummaryDto();
        // 재사용을 확인할 저장된 목표 권수 설정
        summary.setWeekGoalCnt(3);
        // 서비스가 반환한 독서 요약 성공 응답 생성
        ResultData savedResult = ResultData.success(summary);
        // 목표 저장 또는 복사 서비스의 성공 응답 설정
        setGoalResult(copy, savedResult);
        // 최종 응답에 결합할 소셜 통계 생성
        SocialDto.ProfileStatsDto stats = new SocialDto.ProfileStatsDto();
        // 소셜 집계에서 반환한 완료 독서 권수 설정
        stats.setTotalReadBookCnt(8);
        // 소셜 집계에서 반환한 팔로잉 수 설정
        stats.setFollowingCnt(2);
        // 소셜 집계에서 반환한 팔로워 수 설정
        stats.setFollowerCnt(4);
        // 소셜 집계에서 반환한 받은 좋아요 수 설정
        stats.setReceivedLikeCnt(6);
        // 기존 소셜 통계 조회 응답 설정
        when(socialService.getMyPageProfileStats(31L)).thenReturn(ResultData.success(stats));

        // 목표 저장 또는 복사 컨트롤러 실행
        ResultData result = getGoalResult(copy);

        // 기존 성공 응답 코드 유지 확인
        assertEquals(200, result.getCode());
        // 새로운 조회 없이 기존 요약 객체 사용 확인
        assertSame(summary, result.getData());
        // 저장된 목표 권수 유지 확인
        assertEquals(3, summary.getWeekGoalCnt());
        // 소셜 완료 독서 권수 결합 확인
        assertEquals(8, summary.getTotalReadBookCnt());
        // 팔로잉 수 결합 확인
        assertEquals(2, summary.getFollowingCnt());
        // 팔로워 수 결합 확인
        assertEquals(4, summary.getFollowerCnt());
        // 받은 좋아요 수 결합 확인
        assertEquals(6, summary.getReceivedLikeCnt());
        // 저장·복사 후 중복 독서 요약 조회 방지 확인
        verify(reportService, never()).getMonthlyReadingSummary(31L, null);
        // 소셜 통계를 한 번만 조회하는지 확인
        verify(socialService).getMyPageProfileStats(31L);
    }

    /**
     * 목표 저장·복사 실패 응답을 후속 조회 없이 전달하는지 검증
     *
     * @author SeungHyeon.Kang
     * @param copy 이전 목표 복사 여부
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void goalFailureIsPreserved(boolean copy) {

        // 메시지 소스에 의존하지 않는 업무 실패 응답 대역 생성
        ResultData failure = mock(ResultData.class);
        // 기존 업무 실패 코드 설정
        when(failure.getCode()).thenReturn(2020);
        // 저장 또는 복사 실패 응답 설정
        setGoalResult(copy, failure);

        // 원래 실패 객체가 변경 없이 전달되는지 확인
        assertSame(failure, getGoalResult(copy));
        // 실패 요청의 중복 독서 요약 조회 방지 확인
        verify(reportService, never()).getMonthlyReadingSummary(31L, null);
        // 저장 실패 이후 소셜 통계 조회 생략 확인
        verify(socialService, never()).getMyPageProfileStats(31L);
    }

    /**
     * 성공한 목표 저장 뒤 소셜 통계 조회 실패를 기존처럼 전달하는지 검증
     *
     * @author SeungHyeon.Kang
     * @param copy 이전 목표 복사 여부
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void statsFailureIsPreserved(boolean copy) {

        // 저장된 목표 요약 생성
        MonthlyReadingSummaryDto summary = new MonthlyReadingSummaryDto();
        // 목표 저장 또는 복사 성공 응답 설정
        setGoalResult(copy, ResultData.success(summary));
        // 소셜 통계 조회 실패 응답 대역 생성
        ResultData failure = mock(ResultData.class);
        // 소셜 통계 업무 실패 코드 설정
        when(failure.getCode()).thenReturn(2020);
        // 기존 소셜 통계 실패 응답 설정
        when(socialService.getMyPageProfileStats(31L)).thenReturn(failure);

        // 소셜 통계 조회 실패 객체의 변경 없는 전달 확인
        assertSame(failure, getGoalResult(copy));
        // 실패 시에도 중복 독서 요약 조회 방지 확인
        verify(reportService, never()).getMonthlyReadingSummary(31L, null);
    }

    /**
     * 저장 또는 복사 서비스가 반환할 응답 설정
     *
     * @author SeungHyeon.Kang
     * @param copy 이전 목표 복사 여부
     * @param result 서비스 응답
     */
    private void setGoalResult(boolean copy, ResultData result) {

        // 이전 목표 복사 요청의 서비스 응답 설정
        if (copy) {
            // 복사 서비스가 이미 조회한 요약 또는 실패 결과 반환
            when(reportService.copyPreviousReadingGoal(31L)).thenReturn(result);
        }

        // 목표 직접 저장 요청의 서비스 응답 설정
        else {
            // 직접 저장 서비스가 이미 조회한 요약 또는 실패 결과 반환
            when(reportService.setReadingGoal(eq(31L), any(ReadingGoalDto.class))).thenReturn(result);
        }
    }

    /**
     * 목표 저장 또는 복사 컨트롤러 실행
     *
     * @author SeungHyeon.Kang
     * @param copy 이전 목표 복사 여부
     * @return 컨트롤러 응답
     */
    private ResultData getGoalResult(boolean copy) {

        // 요청 종류에 맞는 기존 컨트롤러 응답 반환
        return copy ? controller.copyPreviousReadingGoal(31L)
                : controller.setReadingGoal(31L, new ReadingGoalDto());
    }
}
