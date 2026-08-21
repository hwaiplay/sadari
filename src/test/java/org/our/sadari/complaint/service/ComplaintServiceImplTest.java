package org.our.sadari.complaint.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import org.our.sadari.global.common.util.MessageUtils;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DuplicateKeyException;

/**
 * fileName       : ComplaintServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 대상 원문 스냅샷 저장과 대상 검증을 확인한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    // 신고 데이터 접근 Mock
    @Mock
    private ComplaintMapper complaintMapper;
    // 신고 접수 서비스 단위 테스트 대상
    private ComplaintServiceImpl complaintService;

    /** 각 테스트에 독립된 신고 접수 서비스를 생성한다. */
    @BeforeEach
    void setUp() {
        // 실제 공통 실패 메시지를 사용할 메시지 소스를 생성한다
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 테스트 메시지 프로퍼티 기준을 설정한다
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 인코딩을 설정한다
        messageSource.setDefaultEncoding("UTF-8");
        // 실패 응답이 실제 메시지 소스를 조회하도록 정적 객체를 초기화한다
        new MessageUtils().setMessageSource(messageSource);
        // 신고 접수 서비스 단위 테스트 대상을 생성한다
        complaintService = new ComplaintServiceImpl(complaintMapper);
    }

    /** 화면 본문이 아니라 서버가 조회한 독후감 원문을 신고 스냅샷으로 저장한다. */
    @Test
    void setComplaintStoresServerReportSnapshot() {
        // 독후감 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_REPORT, "CMPL_ABUSE", "상세 사유"
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_REPORT)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                "CMPL_ABUSE")).thenReturn(1);
        // 원본 독후감 테이블에서 조회한 실제 소유자와 본문을 설정한다
        when(complaintMapper.getReportTargetDtl(31L, 7L)).thenReturn(createTarget(22L, "서버 원본 독후감"));
        // DB 자동 생성 신고번호가 DTO에 반영되는 동작을 설정한다
        doAnswer(invocation -> {
            ComplaintDto complaint = invocation.getArgument(0);
            complaint.setCmplNumb(91L);
            return 1;
        }).when(complaintMapper).setComplaint(any(ComplaintDto.class), eq(7L));

        // 독후감 신고 접수를 요청한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 저장된 신고 값을 확인할 캡처 객체를 생성한다
        ArgumentCaptor<ComplaintDto> complaintCaptor = ArgumentCaptor.forClass(ComplaintDto.class);
        // 신고 이력 저장 호출값을 캡처한다
        verify(complaintMapper).setComplaint(complaintCaptor.capture(), eq(7L));
        // 접수된 신고 번호가 성공 응답으로 반환되는지 확인한다
        assertEquals(200, result.getCode());
        assertEquals(91L, result.getData());
        // 클라이언트 화면값이 아닌 서버 조회 원문이 저장되는지 확인한다
        assertEquals(22L, complaintCaptor.getValue().getTagtUser());
        assertEquals("서버 원본 독후감", complaintCaptor.getValue().getTagtCntn());
        assertEquals("상세 사유", complaintCaptor.getValue().getCmplCntn());
    }

    /** 삭제되었거나 본인 소유여서 서버가 조회하지 못한 대상은 신고를 저장하지 않는다. */
    @Test
    void setComplaintRejectsUnavailableTarget() {
        // 댓글 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_REPLY, "CMPL_SPAM", null
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_REPLY)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                "CMPL_SPAM")).thenReturn(1);
        // 신고할 수 없는 댓글은 원문 조회 결과가 없도록 설정한다
        when(complaintMapper.getReplyTargetDtl(31L, 7L)).thenReturn(null);

        // 신고할 수 없는 댓글 접수를 요청한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 저장 거절 응답과 신고 미저장을 확인한다
        assertEquals(ResultEnum.COMMON_SAVE_REJECTED.getCode(), result.getCode());
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class), eq(7L));
    }

    /** 기타 신고 사유에 상세 내용이 없으면 대상 원문을 조회하거나 저장하지 않는다. */
    @Test
    void setComplaintRequiresOtherReasonContent() {
        // 상세 내용이 없는 기타 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_REPORT, Constant.COMPLAINT_REASON_OTHER, "  "
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_REPORT)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                Constant.COMPLAINT_REASON_OTHER)).thenReturn(1);

        // 상세 내용이 없는 기타 신고 접수를 요청한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 유효하지 않은 요청 응답과 신고 미저장을 확인한다
        assertEquals(ResultEnum.COMMON_INVALID_REQUEST.getCode(), result.getCode());
        verify(complaintMapper, never()).getReportTargetDtl(31L, 7L);
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class), eq(7L));
    }

    /** 이미 접수된 동일 사용자와 대상의 신고는 원문 조회와 저장 전에 차단한다. */
    @Test
    void dupComplaintRejectsStored() {
        // 기존 신고와 같은 독후감 대상의 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_REPORT, "CMPL_ABUSE", null
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_REPORT)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                "CMPL_ABUSE")).thenReturn(1);
        // 동일 사용자와 대상의 과거 신고가 존재하도록 설정한다
        when(complaintMapper.dupComplaint(7L, Constant.COMPLAINT_TARGET_REPORT, 31L)).thenReturn(1);

        // 이미 신고한 독후감의 재신고를 요청한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 중복 신고 전용 응답과 원문 미조회 및 신고 미저장을 확인한다
        assertEquals(ResultEnum.COMPLAINT_DUPLICATED.getCode(), result.getCode());
        assertEquals("동일한 대상은 다시 신고할 수 없어요.", result.getMessage());
        verify(complaintMapper, never()).getReportTargetDtl(31L, 7L);
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class), eq(7L));
    }

    /** 사전 중복 조회 직후 발생한 동시 저장 충돌도 중복 신고 응답으로 변환한다. */
    @Test
    void setComplaintHandlesRace() {
        // 동시에 접수될 독후감 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_REPORT, "CMPL_ABUSE", null
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_REPORT)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                "CMPL_ABUSE")).thenReturn(1);
        // 사전 중복 조회 뒤 저장할 대상 원문을 설정한다
        when(complaintMapper.getReportTargetDtl(31L, 7L)).thenReturn(createTarget(22L, "서버 원본 독후감"));
        // 다른 요청이 먼저 저장해 DB 고유 제약 충돌이 발생하도록 설정한다
        when(complaintMapper.setComplaint(any(ComplaintDto.class), eq(7L)))
                .thenThrow(new DuplicateKeyException("duplicate complaint target"));

        // 사전 조회와 저장 사이에 선행 요청이 완료된 신고 접수를 요청한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 동시 저장 충돌도 중복 신고 전용 응답으로 변환되는지 확인한다
        assertEquals(ResultEnum.COMPLAINT_DUPLICATED.getCode(), result.getCode());
    }

    /**
     * 공통 테스트에 사용할 신고 요청을 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param cmplRson 신고 사유
     * @param cmplCntn 신고 상세 내용
     * @return 신고 요청 DTO
     */
    private ComplaintCreateDto createRequest(String tagtType, String cmplRson, String cmplCntn) {

        ComplaintCreateDto request = new ComplaintCreateDto();
        request.setTagtType(tagtType);
        request.setTagtNumb(31L);
        request.setCmplRson(cmplRson);
        request.setCmplCntn(cmplCntn);
        // 입력받은 신고 테스트 요청을 반환한다
        return request;
    }

    /** 신고 대상 소유자와 내용 스냅샷을 담은 조회 결과를 생성한다. */
    private ComplaintDto createTarget(Long tagtUser, String tagtCntn) {

        // 신고 시점에 확정한 대상 정보를 담을 객체를 생성한다
        ComplaintDto target = new ComplaintDto();
        // 관리자 사용자 상세에서 받은 신고를 연결할 대상 소유자를 설정한다
        target.setTagtUser(tagtUser);
        // 신고 접수 뒤 원본이 변경되어도 보존할 대상 내용을 설정한다
        target.setTagtCntn(tagtCntn);
        // 신고 대상 조회 결과를 반환한다
        return target;
    }
}
