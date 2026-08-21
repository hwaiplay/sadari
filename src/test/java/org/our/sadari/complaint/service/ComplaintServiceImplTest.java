package org.our.sadari.complaint.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.complaint.dto.ComplaintDto;
import org.our.sadari.complaint.mapper.ComplaintMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : ComplaintServiceImplTest
 * author         : HanWon.Jang
 * date           : 2026-08-21
 * description    : 독후감과 댓글 신고의 등록 및 차단 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-21        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    // 신고 데이터 접근 Mock
    @Mock
    private ComplaintMapper complaintMapper;
    // 신고 상세 내용 비속어 검사 서비스 Mock
    @Mock
    private BadWordDetectionService badWordDetectionService;
    // 신고 서비스 단위 테스트 대상
    private ComplaintServiceImpl complaintService;

    /** 각 테스트에 독립된 신고 서비스를 생성한다. */
    @BeforeEach
    void setUp() {
        // 실제 다국어 프로퍼티를 조회할 메시지 소스를 생성한다
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 공통 메시지 프로퍼티를 테스트 조회 기준으로 설정한다
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 인코딩을 설정한다
        messageSource.setDefaultEncoding("UTF-8");
        // 실패 응답이 실제 공통 메시지를 사용하도록 정적 조회 객체를 초기화한다
        new MessageUtils().setMessageSource(messageSource);
        // 신고 서비스 단위 테스트 대상을 생성한다
        complaintService = new ComplaintServiceImpl(complaintMapper, badWordDetectionService);
    }

    /** 활성 회원의 공개 독후감 신고를 접수한다. */
    @Test
    void setComplaintCreatesReportComplaint() {
        // 독후감 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(Constant.COMPLAINT_TARGET_REPORT, "CMPL_SPAM", null);
        // 신고자와 사유 및 대상이 유효한 조건을 설정한다
        setValidCommonCondition();
        when(complaintMapper.getReportOwnerNumb(31L)).thenReturn(8L);
        when(complaintMapper.getDupComplaintCnt(any(ComplaintDto.class))).thenReturn(0);
        when(complaintMapper.setComplaint(any(ComplaintDto.class))).thenAnswer(invocation -> {
            ComplaintDto complaint = invocation.getArgument(0);
            complaint.setCmplNumb(51L);
            return 1;
        });

        // 독후감 신고를 접수한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 생성된 신고 번호가 성공 응답으로 반환되는지 확인한다
        assertEquals(200, result.getCode());
        assertEquals(51L, result.getData());
        ArgumentCaptor<ComplaintDto> captor = ArgumentCaptor.forClass(ComplaintDto.class);
        verify(complaintMapper).setComplaint(captor.capture());
        assertEquals("CMPL_SPAM", captor.getValue().getCmplRson());
    }

    /** 본인이 작성한 댓글 신고를 저장하지 않는다. */
    @Test
    void setComplaintRejectsOwnReply() {
        ComplaintCreateDto request = createRequest(Constant.COMPLAINT_TARGET_REPLY, "CMPL_ABUSE", null);
        setValidCommonCondition();
        when(complaintMapper.getReplyOwnerNumb(31L)).thenReturn(7L);

        ResultData result = complaintService.setComplaint(7L, request);

        assertEquals(ResultEnum.COMPLAINT_SELF_REJECTED.getCode(), result.getCode());
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class));
    }

    /** 같은 콘텐츠를 이미 신고한 사용자의 재신고를 저장하지 않는다. */
    @Test
    void setComplaintRejectsDuplicate() {
        ComplaintCreateDto request = createRequest(Constant.COMPLAINT_TARGET_REPORT, "CMPL_PRIVACY", null);
        setValidCommonCondition();
        when(complaintMapper.getReportOwnerNumb(31L)).thenReturn(8L);
        when(complaintMapper.getDupComplaintCnt(any(ComplaintDto.class))).thenReturn(1);

        ResultData result = complaintService.setComplaint(7L, request);

        assertEquals(ResultEnum.COMPLAINT_DUPLICATED.getCode(), result.getCode());
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class));
    }

    /** 비활성 계정의 신고를 대상 조회 전에 차단한다. */
    @Test
    void setComplaintRejectsInactiveUser() {
        ComplaintCreateDto request = createRequest(Constant.COMPLAINT_TARGET_REPORT, "CMPL_SPAM", null);
        when(complaintMapper.getActiveUserNumbForUpdate(7L)).thenReturn(null);

        ResultData result = complaintService.setComplaint(7L, request);

        assertEquals(ResultEnum.FORBIDDEN.getCode(), result.getCode());
        verify(complaintMapper, never()).getReportOwnerNumb(31L);
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class));
    }

    /** 기타 사유의 상세 설명이 없으면 신고를 저장하지 않는다. */
    @Test
    void setComplaintRequiresOtherDetail() {
        ComplaintCreateDto request = createRequest(Constant.COMPLAINT_TARGET_REPORT,
                Constant.COMPLAINT_REASON_OTHER, "   ");
        setValidCommonCondition();

        ResultData result = complaintService.setComplaint(7L, request);

        assertEquals(ResultEnum.COMMON_INVALID_REQUEST.getCode(), result.getCode());
        verify(complaintMapper, never()).getReportOwnerNumb(31L);
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class));
    }

    /** 공통으로 유효한 활성 신고자와 신고 사유 조건을 설정한다. */
    private void setValidCommonCondition() {
        when(complaintMapper.getActiveUserNumbForUpdate(7L)).thenReturn(7L);
        when(complaintMapper.getComplaintReasonCnt(any(String.class))).thenReturn(1);
    }

    /** 테스트용 신고 요청을 생성한다. */
    private ComplaintCreateDto createRequest(String targetType, String reason, String detail) {
        ComplaintCreateDto request = new ComplaintCreateDto();
        request.setTagtType(targetType);
        request.setTagtNumb(31L);
        request.setCmplRson(reason);
        request.setCmplCntn(detail);
        return request;
    }
}
