package org.our.sadari.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.book.mapper.BookMapper;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.myPage.dto.ReadingSummaryQueryDto;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.report.mapper.ReportMapper;
import org.our.sadari.social.mapper.SocialMapper;

/**
 * fileName       : ReportServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-04
 * description    : 독서 요약 조회의 공개 범위 전달 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-04        SeungHyeon.Kang       최초 생성
 * 2026-08-14        SeungHyeon.Kang    독후감 참조 데이터 삭제 순서 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    // Report 데이터 접근 객체
    @Mock
    private ReportMapper reportMapper;

    // Social 데이터 접근 객체
    @Mock
    private SocialMapper socialMapper;

    // Book 데이터 접근 객체
    @Mock
    private BookMapper bookMapper;

    // 공통코드 캐시 조회 객체
    @Mock
    private CodeUtil codeUtil;

    // 비속어 검사 서비스
    @Mock
    private BadWordDetectionService badWordDetectionService;

    // 독서 요약 서비스 단위 테스트 대상
    private ReportServiceImpl reportService;

    /**
     * 각 테스트가 독립된 Mock 의존성을 사용하는 독후감 서비스 구현체를 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 독서 요약 서비스 단위 테스트 대상을 생성한다
        reportService = new ReportServiceImpl(reportMapper, socialMapper, bookMapper, codeUtil, badWordDetectionService);
        // 독서 요약 집계 SQL이 빈 기본 집계 결과를 반환하도록 설정한다
        lenient().when(reportMapper.getReadingSummary(any(ReadingSummaryQueryDto.class)))
                .thenReturn(new ReadingSummaryQueryDto());
        // 독서 요약 목록 SQL이 빈 목록을 반환하도록 설정한다
        lenient().when(reportMapper.getReadingSummaryList(any(ReadingSummaryQueryDto.class)))
                .thenReturn(List.of());
    }

    /**
     * 다른 사용자 소셜 요약이 집계와 목록 SQL 모두에 공개 독후감 조건을 전달하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getSocialSummaryPublic() {
        // 소셜 프로필과 같은 공개 범위로 독서 요약을 조회한다
        reportService.getMonthlyReadingSummary(31L, Constant.COMM_YES);

        // 집계 SQL에 전달된 공개 범위 조건을 확인할 인자 Capture를 생성한다
        ArgumentCaptor<ReadingSummaryQueryDto> summaryCaptor = ArgumentCaptor.forClass(ReadingSummaryQueryDto.class);
        // 목록 SQL에 전달된 공개 범위 조건을 확인할 인자 Capture를 생성한다
        ArgumentCaptor<ReadingSummaryQueryDto> reportListCaptor = ArgumentCaptor.forClass(ReadingSummaryQueryDto.class);
        // 독서량과 목표 달성 집계 SQL의 조회 조건을 Capture한다
        verify(reportMapper).getReadingSummary(summaryCaptor.capture());
        // 현재 읽는 책과 완료 독후감 목록 SQL의 조회 조건을 Capture한다
        verify(reportMapper).getReadingSummaryList(reportListCaptor.capture());

        // 집계 SQL이 공개 독후감만 계산하도록 공개 여부가 전달되었는지 확인한다
        assertEquals(Constant.COMM_YES, summaryCaptor.getValue().getPubcYsno());
        // 목록 SQL이 공개 독후감만 반환하도록 공개 여부가 전달되었는지 확인한다
        assertEquals(Constant.COMM_YES, reportListCaptor.getValue().getPubcYsno());
    }

    /**
     * 본인 마이페이지 요약은 공개 여부와 관계없이 기존 전체 독후감 범위를 유지하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getMySummaryKeepsAll() {
        // 마이페이지와 같은 전체 범위로 독서 요약을 조회한다
        reportService.getMonthlyReadingSummary(31L, null);

        // 집계 SQL에 전달된 전체 범위 조건을 확인할 인자 Capture를 생성한다
        ArgumentCaptor<ReadingSummaryQueryDto> summaryCaptor = ArgumentCaptor.forClass(ReadingSummaryQueryDto.class);
        // 목록 SQL에 전달된 전체 범위 조건을 확인할 인자 Capture를 생성한다
        ArgumentCaptor<ReadingSummaryQueryDto> reportListCaptor = ArgumentCaptor.forClass(ReadingSummaryQueryDto.class);
        // 독서량과 목표 달성 집계 SQL의 조회 조건을 Capture한다
        verify(reportMapper).getReadingSummary(summaryCaptor.capture());
        // 현재 읽는 책과 완료 독후감 목록 SQL의 조회 조건을 Capture한다
        verify(reportMapper).getReadingSummaryList(reportListCaptor.capture());

        // 집계 SQL의 공개 여부 조건이 비어 있어 본인 전체 독후감을 유지하는지 확인한다
        assertNull(summaryCaptor.getValue().getPubcYsno());
        // 목록 SQL의 공개 여부 조건이 비어 있어 본인 전체 독후감을 유지하는지 확인한다
        assertNull(reportListCaptor.getValue().getPubcYsno());
    }

    /**
     * 독후감 삭제 시 외래키 참조 데이터가 부모 독후감보다 먼저 삭제되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delReportDeletesReferencesFirst() {
        // 부모 독후감 삭제가 성공하는 조건을 설정한다
        when(reportMapper.delReport(any(ReportDto.class))).thenReturn(1);

        // 댓글이 연결된 독후감 삭제를 요청한다
        ResultData result = reportService.delReport(7L, 31L);

        // 독후감 삭제 성공 응답이 반환되는지 확인한다
        assertEquals(200, result.getCode());
        // 외래키와 공용 대상 데이터를 정리하는 호출 순서를 검증할 객체를 생성한다
        InOrder deleteOrder = inOrder(reportMapper);
        // 댓글 대상 좋아요가 댓글보다 먼저 삭제되는지 확인한다
        deleteOrder.verify(reportMapper).delReportReplyLikes(any(ReportDto.class));
        // 대댓글이 최상위 댓글보다 먼저 삭제되는지 확인한다
        deleteOrder.verify(reportMapper).delReportChildReplies(any(ReportDto.class));
        // 대댓글 정리 뒤 나머지 댓글이 삭제되는지 확인한다
        deleteOrder.verify(reportMapper).delReportReplies(any(ReportDto.class));
        // 독후감 대상 좋아요가 부모 독후감보다 먼저 삭제되는지 확인한다
        deleteOrder.verify(reportMapper).delReportLikes(any(ReportDto.class));
        // 모든 참조 데이터가 정리된 뒤 부모 독후감이 삭제되는지 확인한다
        deleteOrder.verify(reportMapper).delReport(any(ReportDto.class));
    }
}
