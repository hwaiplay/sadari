package org.our.sadari.complaint.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.our.sadari.complaint.config.ComplaintAutoActionProperties;
import org.our.sadari.complaint.dto.ComplaintActionDto;
import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.complaint.dto.ComplaintDto;
import org.our.sadari.complaint.mapper.ComplaintMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.StoredFile;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DuplicateKeyException;

/**
 * fileName       : ComplaintServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 대상 원문 저장과 누적 자동 조치를 확인한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    버전별 자동 조치·이미지 증거와 입력 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    // 신고 데이터 접근 Mock
    @Mock
    private ComplaintMapper complaintMapper;
    // 프로필 사진 파일 정리 서비스 Mock
    @Mock
    private FileService fileService;
    // 프로필 사진 실제 원본 저장소 Mock
    @Mock
    private FileStorage fileStorage;
    // 신고 상세 비속어 검사 서비스 Mock
    @Mock
    private BadWordDetectionService badWordDetectionService;
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
        // 대상별 기본 임계치가 5건인 자동 조치 설정을 생성한다
        ComplaintAutoActionProperties properties = new ComplaintAutoActionProperties();
        // 신고 접수 서비스 단위 테스트 대상을 생성한다
        complaintService = new ComplaintServiceImpl(complaintMapper, properties, fileService, fileStorage, badWordDetectionService);
    }

    /**
     * 신고 상세 내용이 500자를 초과하면 대상 원문을 조회하거나 저장하지 않는다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setComplaintRejectsLong() {
        // 서버 최대 길이보다 한 글자 긴 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_REPORT, "CMPL_ABUSE", "가".repeat(501)
        );
        // 신고 접수가 가능한 활성 회원 상태를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);

        // 500자를 초과한 신고 접수를 요청한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 500자를 초과한 요청이 공통 유효성 실패 코드로 거절되는지 확인한다
        assertEquals(ResultEnum.COMMON_INVALID_REQUEST.getCode(), result.getCode());
        // 유효하지 않은 상세 내용으로 대상 원문을 조회하지 않는지 확인한다
        verify(complaintMapper, never()).getReportTargetDtl(31L, 7L);
        // 유효하지 않은 상세 내용으로 신고 이력을 저장하지 않는지 확인한다
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class), eq(7L));
    }

    /**
     * 신고 상세 내용에 비속어가 포함되면 대상 원문을 조회하거나 저장하지 않는다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setComplaintBlocksBadWord() {
        // 비속어가 포함된 상세 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_REPORT, "CMPL_ABUSE", "비속어가 포함된 신고 내용"
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_REPORT)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                "CMPL_ABUSE")).thenReturn(1);
        // 상세 내용에서 차단할 비속어가 탐지되는 조건을 설정한다
        when(badWordDetectionService.findBadWord("비속어가 포함된 신고 내용"))
                .thenReturn(java.util.Optional.of("비속어"));

        // 비속어가 포함된 신고 접수를 요청한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 비속어가 포함된 요청이 공통 비속어 실패 코드로 거절되는지 확인한다
        assertEquals(ResultEnum.COMMON_BAD_WORD_INCLUDED.getCode(), result.getCode());
        // 차단된 상세 내용으로 대상 원문을 조회하지 않는지 확인한다
        verify(complaintMapper, never()).getReportTargetDtl(31L, 7L);
        // 차단된 상세 내용으로 신고 이력을 저장하지 않는지 확인한다
        verify(complaintMapper, never()).setComplaint(any(ComplaintDto.class), eq(7L));
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
        assertEquals(64, complaintCaptor.getValue().getTagtHash().length());
        assertEquals("상세 사유", complaintCaptor.getValue().getCmplCntn());
    }

    /** 같은 독후감 버전의 유효 신고가 5건 누적되면 비공개로 전환하고 조치 이력을 저장한다. */
    @Test
    void setComplaintDeletesReportAtThreshold() {
        // 다섯 번째 독후감 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_REPORT, "CMPL_ABUSE", null
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_REPORT)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                "CMPL_ABUSE")).thenReturn(1);
        // 잠금 조회한 독후감 원본과 소유자를 설정한다
        when(complaintMapper.getReportTargetDtl(31L, 7L)).thenReturn(createTarget(22L, "신고 대상 독후감"));
        // 신규 신고 번호가 자동 조치 발생 신고로 연결되도록 설정한다
        doAnswer(invocation -> {
            ComplaintDto complaint = invocation.getArgument(0);
            complaint.setCmplNumb(95L);
            return 1;
        }).when(complaintMapper).setComplaint(any(ComplaintDto.class), eq(7L));
        // 신규 신고를 포함한 반려 제외 누적 건수가 임계치에 도달하도록 설정한다
        when(complaintMapper.getAutoActionCmplCnt(
                eq(Constant.COMPLAINT_TARGET_REPORT), eq(31L), anyString())).thenReturn(5);
        // 잠금 조회한 독후감 한 건이 비공개로 전환되도록 설정한다
        when(complaintMapper.uptAutoReportPrivate(31L, 22L)).thenReturn(1);
        // 자동 조치 결과 이력 한 건이 저장되도록 설정한다
        when(complaintMapper.setAutoAction(any(ComplaintActionDto.class))).thenReturn(1);
        // 자동 조치와 연결된 미처리 신고가 종결되도록 설정한다
        when(complaintMapper.uptAutoComplaints(
                eq(Constant.COMPLAINT_TARGET_REPORT),
                eq(31L),
                anyString(),
                eq("동일 버전 누적 신고 5건에 따른 독후감 비공개 전환")
        )).thenReturn(5);

        // 다섯 번째 독후감 신고를 접수한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 자동 조치 결과를 확인할 캡처 객체를 생성한다
        ArgumentCaptor<ComplaintActionDto> actionCaptor = ArgumentCaptor.forClass(ComplaintActionDto.class);
        // 독후감 원본과 연결 데이터를 삭제하지 않고 공개 여부만 변경하는지 확인한다
        verify(complaintMapper).uptAutoReportPrivate(31L, 22L);
        // 자동 조치 결과 이력의 저장값을 캡처한다
        verify(complaintMapper).setAutoAction(actionCaptor.capture());
        // 관련 미처리 신고가 조치 완료 상태로 변경되는지 확인한다
        verify(complaintMapper).uptAutoComplaints(
                Constant.COMPLAINT_TARGET_REPORT,
                31L,
                actionCaptor.getValue().getTagtHash(),
                "동일 버전 누적 신고 5건에 따른 독후감 비공개 전환"
        );
        // 신고 접수 성공과 첫 번째 5건 단위 조치 이력을 확인한다
        assertEquals(200, result.getCode());
        assertEquals(Constant.COMPLAINT_ACTION_HIDE_REPORT, actionCaptor.getValue().getActnType());
        assertEquals(Constant.COMPLAINT_RESULT_APPLIED, actionCaptor.getValue().getRsltCode());
        assertEquals(5, actionCaptor.getValue().getThrsCntt());
        assertEquals(5, actionCaptor.getValue().getCmplCntt());
        assertEquals(1, actionCaptor.getValue().getActnOrdr());
        assertEquals(95L, actionCaptor.getValue().getTrigCmpl());
    }

    /** 프로필 사진의 유효 신고가 5건 누적되면 기본 이미지로 변경하고 파일을 커밋 후 정리한다. */
    @Test
    void setComplaintResetsProfileAtThreshold() throws java.io.IOException {
        // 다섯 번째 프로필 사진 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_PROFILE, "CMPL_PRIVACY", null
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_PROFILE)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                "CMPL_PRIVACY")).thenReturn(1);
        // 현재 프로필 사진 파일과 대상 사용자를 잠금 조회한 결과를 생성한다
        ComplaintDto target = createTarget(31L, "프로필 사진: unsafe.jpg");
        // 자동 조치 뒤 정리할 현재 프로필 사진 파일 번호를 설정한다
        target.setFileNumb(501L);
        // 실제 증거 원본을 읽을 내부 저장소 경로와 파일명을 설정한다
        target.setFilePath("/uploads/profile/260822/unsafe.jpg");
        target.setStorName("unsafe.jpg");
        target.setOrigName("unsafe.jpg");
        // 현재 프로필 사진 신고 대상을 설정한다
        when(complaintMapper.getProfileTargetDtl(31L, 7L)).thenReturn(target);
        // 신고 시점의 실제 프로필 이미지 원본을 저장소에서 조회하도록 설정한다
        when(fileStorage.getFile("profile/260822/unsafe.jpg"))
                .thenReturn(java.util.Optional.of(new StoredFile(new byte[]{1, 2, 3}, "image/jpeg")));
        // 동일 이미지 버전의 기존 증거가 없어 신규 원본을 저장하도록 설정한다
        when(complaintMapper.getEvidenceNumb(eq(Constant.COMPLAINT_TARGET_PROFILE)
                , eq(31L), anyString())).thenReturn(null);
        // 관리자 전용 이미지 증거 번호가 신규 저장 뒤 DTO에 반영되도록 설정한다
        doAnswer(invocation -> {
            org.our.sadari.complaint.dto.ComplaintEvidenceDto evidence = invocation.getArgument(0);
            evidence.setEvdcNumb(801L);
            return 1;
        }).when(complaintMapper).setEvidence(any(org.our.sadari.complaint.dto.ComplaintEvidenceDto.class));
        // 신규 신고 번호가 자동 조치 결과와 연결되도록 설정한다
        doAnswer(invocation -> {
            ComplaintDto complaint = invocation.getArgument(0);
            complaint.setCmplNumb(96L);
            return 1;
        }).when(complaintMapper).setComplaint(any(ComplaintDto.class), eq(7L));
        // 반려 제외 누적 신고가 임계치에 도달하도록 설정한다
        when(complaintMapper.getAutoActionCmplCnt(
                eq(Constant.COMPLAINT_TARGET_PROFILE), eq(31L), anyString())).thenReturn(5);
        // 프로필 사진 참조 한 건이 제거되도록 설정한다
        when(complaintMapper.uptAutoProfile(31L)).thenReturn(1);
        // 자동 조치 결과 이력 한 건이 저장되도록 설정한다
        when(complaintMapper.setAutoAction(any(ComplaintActionDto.class))).thenReturn(1);
        // 자동 조치와 연결된 미처리 신고가 종결되도록 설정한다
        when(complaintMapper.uptAutoComplaints(
                eq(Constant.COMPLAINT_TARGET_PROFILE),
                eq(31L),
                anyString(),
                eq("누적 신고 5건에 따른 프로필 사진 기본 이미지 초기화")
        )).thenReturn(5);

        // 다섯 번째 프로필 사진 신고를 접수한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 프로필 사진 참조 제거와 커밋 후 파일 정리가 연결되는지 확인한다
        verify(complaintMapper).uptAutoProfile(31L);
        verify(fileService).delFile(501L);
        // 프로필 사진 자동 조치가 성공해 신고 번호를 반환하는지 확인한다
        assertEquals(200, result.getCode());
        assertEquals(96L, result.getData());
    }

    /** 배경사진의 유효 신고가 5건 누적되면 프로필과 독립적으로 기본 배경 상태로 변경한다. */
    @Test
    void setComplaintResetsBackgroundAtThreshold() throws java.io.IOException {
        // 다섯 번째 배경사진 신고 요청을 생성한다
        ComplaintCreateDto request = createRequest(
                Constant.COMPLAINT_TARGET_BACKGROUND, "CMPL_PRIVACY", null
        );
        // 활성 신고자와 유효한 대상 및 사유 코드를 설정한다
        when(complaintMapper.getUserStat(7L)).thenReturn(Constant.USER_STAT_ACTIVE);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET,
                Constant.COMPLAINT_TARGET_BACKGROUND)).thenReturn(1);
        when(complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON,
                "CMPL_PRIVACY")).thenReturn(1);
        // 현재 배경사진 파일과 대상 사용자를 잠금 조회한 결과를 생성한다
        ComplaintDto target = createTarget(31L, "배경사진: unsafe-background.jpg");
        target.setFileNumb(502L);
        target.setFilePath("/uploads/background/260822/unsafe-background.jpg");
        target.setStorName("unsafe-background.jpg");
        target.setOrigName("unsafe-background.jpg");
        when(complaintMapper.getBackgroundTargetDtl(31L, 7L)).thenReturn(target);
        // 신고 시점의 실제 배경사진 원본을 저장소에서 조회하도록 설정한다
        when(fileStorage.getFile("background/260822/unsafe-background.jpg"))
                .thenReturn(java.util.Optional.of(new StoredFile(new byte[]{4, 5, 6}, "image/jpeg")));
        when(complaintMapper.getEvidenceNumb(eq(Constant.COMPLAINT_TARGET_BACKGROUND)
                , eq(31L), anyString())).thenReturn(null);
        // 관리자 전용 이미지 증거 번호가 신규 저장 뒤 DTO에 반영되도록 설정한다
        doAnswer(invocation -> {
            org.our.sadari.complaint.dto.ComplaintEvidenceDto evidence = invocation.getArgument(0);
            evidence.setEvdcNumb(802L);
            return 1;
        }).when(complaintMapper).setEvidence(any(org.our.sadari.complaint.dto.ComplaintEvidenceDto.class));
        // 신규 신고 번호가 자동 조치 결과와 연결되도록 설정한다
        doAnswer(invocation -> {
            ComplaintDto complaint = invocation.getArgument(0);
            complaint.setCmplNumb(97L);
            return 1;
        }).when(complaintMapper).setComplaint(any(ComplaintDto.class), eq(7L));
        when(complaintMapper.getAutoActionCmplCnt(
                eq(Constant.COMPLAINT_TARGET_BACKGROUND), eq(31L), anyString())).thenReturn(5);
        when(complaintMapper.uptAutoBackground(31L)).thenReturn(1);
        when(complaintMapper.setAutoAction(any(ComplaintActionDto.class))).thenReturn(1);
        when(complaintMapper.uptAutoComplaints(
                eq(Constant.COMPLAINT_TARGET_BACKGROUND), eq(31L), anyString(),
                eq("누적 신고 5건에 따른 배경사진 기본 이미지 초기화")
        )).thenReturn(5);

        // 다섯 번째 배경사진 신고를 접수한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 배경사진 참조만 제거하고 해당 파일을 정리하는지 확인한다
        verify(complaintMapper).uptAutoBackground(31L);
        verify(complaintMapper, never()).uptAutoProfile(31L);
        verify(fileService).delFile(502L);
        assertEquals(200, result.getCode());
        assertEquals(97L, result.getData());
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

    /** 이미 접수된 동일 사용자와 대상 버전의 신고는 해시 확인 뒤 저장 전에 차단한다. */
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
        // 재신고 대상의 현재 실제 원문을 설정한다
        when(complaintMapper.getReportTargetDtl(31L, 7L)).thenReturn(createTarget(22L, "동일 버전 독후감"));
        // 동일 사용자와 대상 버전의 과거 신고가 존재하도록 설정한다
        when(complaintMapper.dupComplaint(eq(7L), eq(Constant.COMPLAINT_TARGET_REPORT)
                , eq(31L), anyString())).thenReturn(1);

        // 이미 신고한 독후감의 재신고를 요청한다
        ResultData result = complaintService.setComplaint(7L, request);

        // 중복 신고 전용 응답과 신고 미저장을 확인한다
        assertEquals(ResultEnum.COMPLAINT_DUPLICATED.getCode(), result.getCode());
        assertEquals("동일한 대상은 다시 신고할 수 없어요.", result.getMessage());
        verify(complaintMapper).getReportTargetDtl(31L, 7L);
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
