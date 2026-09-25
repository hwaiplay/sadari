package org.our.sadari.global.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.complaint.service.ComplaintEvidenceCleanupService;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.scheduler.service.AlimDeleteService;
import org.our.sadari.global.scheduler.service.ReportDateOverService;
import org.our.sadari.global.scheduler.service.UserHardDeleteService;
import org.our.sadari.global.scheduler.service.UserStatusEventService;
import org.our.sadari.global.scheduler.service.TimerDetailDeleteService;
import org.our.sadari.readingClub.service.OwnerElectionService;
import org.our.sadari.readingClub.service.ReadingClubService;
import org.our.sadari.timer.service.ReadingTimerService;
import org.slf4j.MDC;
import org.junit.jupiter.api.AfterEach;

/**
 * fileName       : SchedulerTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 로직의 동작을 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    회원 상태 Outbox 스케줄러 분기 검증 추가
 * 2026-08-20        SeungHyeon.Kang    타이머 알림 실행 분기 검증
 * 2026-09-05        SeungHyeon.Kang    통합 스케줄 실행 위임 검증
 * 2026-09-19        SeungHyeon.Kang         스케줄 실패 문맥 복원 검증
 */
@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    // ReportDateOver 업무 처리 서비스
    @Mock
    private ReportDateOverService reportDateOverService;

    // AlimDelete 업무 처리 서비스
    @Mock
    private AlimDeleteService alimDeleteService;

    // UserHardDelete 업무 처리 서비스
    @Mock
    private UserHardDeleteService userHardDeleteService;

    // UserStatusEvent 업무 처리 서비스
    @Mock
    private UserStatusEventService userStatusEventService;

    // 독서 타이머 상세 정리 서비스
    @Mock
    private TimerDetailDeleteService timerDetailDeleteService;
    // 독서 타이머 목표시간 알림 업무 처리 서비스
    @Mock
    private ReadingTimerService readingTimerService;

    // 모임 독서 회차 확정 업무 처리 서비스
    @Mock
    private ReadingClubService readingClubService;

    // 모임장 승계 선거 업무 처리 서비스
    @Mock
    private OwnerElectionService ownerElectionService;

    // 프로필 이미지 임시 파일 업무 처리 서비스
    @Mock
    private FileService fileService;

    // 신고 증거 정리 업무 처리 서비스
    @Mock
    private ComplaintEvidenceCleanupService complaintEvidenceCleanupService;

    // 공통코드 캐시 조회 객체
    @Mock
    private CodeUtil codeUtil;

    // 스케줄러 활성화 조건 단위 테스트 대상
    @InjectMocks
    private Scheduler scheduler;

    /** 테스트에서 지정한 스케줄 진단 문맥 정리 */
    @AfterEach
    void clearLogContext() {
        // 다른 테스트에 실행 식별자가 전파되지 않도록 정리
        MDC.clear();
    }

    /** 코드 조회 단계 실패도 실행 식별자로 추적하고 기존 예외 유지 */
    @Test
    void restoresJobOnFailure() {
        // 중첩 실행의 기존 문맥
        MDC.put("jobId", "outer-job");
        // 스케줄 활성 여부 조회 장애
        RuntimeException failure = new IllegalStateException("private-job-data");
        // 업무 진입 전 실패도 진단 문맥이 있는지 검증
        doAnswer(invocation -> {
            // 실제 스케줄 실행 식별자 확인
            assertNotNull(MDC.get("jobId"));
            throw failure;
        }).when(codeUtil).existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_ALIM_DELETE);
        // 실패 예외가 로그 때문에 변경되지 않도록 확인
        assertSame(failure, assertThrows(RuntimeException.class, scheduler::delAlim));
        // 상위 스케줄 문맥 복구 확인
        assertEquals("outer-job", MDC.get("jobId"));
    }

    /**
     * SCHD_CODE의 REPORT_DATE_OVER 상세코드가 사용 중이면 실제 알림 서비스를 호출하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendOverdueWhenEnabled() {
        // existsCode 조회로 대상 데이터의 존재 여부를 확인
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_REPORT_DATE_OVER
        // 테스트 대상 의존 호출에 반환할 값을 지정
        )).thenReturn(true);

        // sendReportDateOverAlim 호출로 검증된 알림 또는 응답을 전송
        scheduler.sendReportDateOverAlim();

        // 의존 객체가 예상한 인자로 호출되었는지 검증
        verify(reportDateOverService).sendReportDateOverAlim();
    }

    /**
     * 상세코드가 사용 중지 상태이거나 존재하지 않으면 실제 알림 서비스를 호출하지 않는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendOverdueSkipsDisabled() {
        // existsCode 조회로 대상 데이터의 존재 여부를 확인
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_REPORT_DATE_OVER
        // 테스트 대상 의존 호출에 반환할 값을 지정
        )).thenReturn(false);

        // sendReportDateOverAlim 호출로 검증된 알림 또는 응답을 전송
        scheduler.sendReportDateOverAlim();

        // 의존 객체가 예상한 인자로 호출되었는지 검증
        verify(reportDateOverService, never()).sendReportDateOverAlim();
    }

    /**
     * ALIM_DELETE 상세코드가 사용 중이면 알림 삭제 서비스를 호출하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delAlimWhenEnabled() {
        // existsCode 조회로 대상 데이터의 존재 여부를 확인
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_ALIM_DELETE
        // 테스트 대상 의존 호출에 반환할 값을 지정
        )).thenReturn(true);

        // delAlim 호출로 삭제 대상 데이터를 정리
        scheduler.delAlim();

        // 의존 객체가 예상한 인자로 호출되었는지 검증
        verify(alimDeleteService).delAlim();
    }

    /**
     * ALIM_DELETE 상세코드가 중지 상태이면 알림 삭제 서비스를 호출하지 않는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delAlimSkipsDisabled() {
        // existsCode 조회로 대상 데이터의 존재 여부를 확인
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_ALIM_DELETE
        // 테스트 대상 의존 호출에 반환할 값을 지정
        )).thenReturn(false);

        // delAlim 호출로 삭제 대상 데이터를 정리
        scheduler.delAlim();

        // 의존 객체가 예상한 인자로 호출되었는지 검증
        verify(alimDeleteService, never()).delAlim();
    }

    /**
     * USER_HARD_DELETE 상세코드가 사용 중이면 영구 삭제 서비스를 호출하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delUsersWhenEnabled() {

        // 영구 삭제 스케줄러 상세코드가 활성 상태인 조건을 설정
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_USER_HARD_DELETE
        )).thenReturn(true);

        // 영구 삭제 대기 회원 스케줄러를 실행
        scheduler.delPendingUsers();

        // 활성 상태에서 영구 삭제 서비스가 호출됐는지 검증
        verify(userHardDeleteService).delPendingUsers();
    }

    /**
     * USER_HARD_DELETE 상세코드가 중지 상태이면 영구 삭제 서비스를 호출하지 않는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delUsersSkipsDisabled() {

        // 영구 삭제 스케줄러 상세코드가 비활성 상태인 조건을 설정
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_USER_HARD_DELETE
        )).thenReturn(false);

        // 영구 삭제 대기 회원 스케줄러를 실행
        scheduler.delPendingUsers();

        // 비활성 상태에서 영구 삭제 서비스가 호출되지 않았는지 검증
        verify(userHardDeleteService, never()).delPendingUsers();
    }

    /**
     * USER_STATUS_SYNC 상세코드가 사용 중이면 회원 상태 Outbox 동기화를 실행하는지 확인
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void syncStatusWhenEnabled() {

        // 회원 상태 동기화 스케줄러 상세코드를 활성 상태로 설정
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_USER_STATUS_SYNC
        )).thenReturn(true);

        // 5분 주기 회원 상태 동기화 스케줄러를 실행
        scheduler.syncUserStatusEvents();

        // 활성 상태에서 Outbox 동기화 서비스가 호출됐는지 확인
        verify(userStatusEventService).syncUserStatusEvents();
    }

    /**
     * USER_STATUS_SYNC 상세코드가 중지 상태이면 회원 상태 Outbox 동기화를 생략하는지 확인
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void syncStatusSkipsDisabled() {

        // 회원 상태 동기화 스케줄러 상세코드를 중지 상태로 설정
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_USER_STATUS_SYNC
        )).thenReturn(false);

        // 5분 주기 회원 상태 동기화 스케줄러를 실행
        scheduler.syncUserStatusEvents();

        // 중지 상태에서 Outbox 동기화 서비스가 호출되지 않았는지 확인
        verify(userStatusEventService, never()).syncUserStatusEvents();
    }

    /**
     * BOOK_TIMER_OVER 상세코드가 사용 중이면 독서 타이머 목표시간 알림을 실행하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendTimerAlimWhenEnabled() {

        // 독서 타이머 알림 스케줄러 상세코드를 활성 상태로 설정
        when(codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_BOOK_TIMER_OVER)).thenReturn(true);
        // 독서 타이머 목표시간 알림 스케줄러를 실행
        scheduler.sendTimerAlim();
        // 활성 상태에서 알림 서비스가 호출됐는지 확인
        verify(readingTimerService).sendTimerAlim();
    }

    /**
     * BOOK_TIMER_OVER 상세코드가 중지 상태이면 독서 타이머 목표시간 알림을 생략하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendTimerAlimSkipsDisabled() {

        // 독서 타이머 알림 스케줄러 상세코드를 중지 상태로 설정
        when(codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_BOOK_TIMER_OVER)).thenReturn(false);
        // 독서 타이머 목표시간 알림 스케줄러를 실행
        scheduler.sendTimerAlim();
        // 중지 상태에서 알림 서비스가 호출되지 않았는지 확인
        verify(readingTimerService, never()).sendTimerAlim();
    }

    /**
     * 종료된 모임 독서 회차 확정을 모임 서비스에 위임하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void completeRoundDelegates() {
        // 통합 스케줄러의 모임 독서 회차 확정 진입점을 실행
        scheduler.completeExpiredRound();
        // 종료 회차 확정 업무가 모임 서비스에 위임됐는지 검증
        verify(readingClubService).completeExpiredRound();
    }

    /**
     * 모임장 승계 선거 생성과 마감을 선거 서비스에 위임하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptElectionDelegates() {
        // 통합 스케줄러의 모임장 승계 선거 진입점을 실행
        scheduler.uptOwnerElection();
        // 누락된 모임장 승계 선거 생성이 위임됐는지 검증
        verify(ownerElectionService).startPendingElection();
        // 마감된 모임장 승계 선거 확정이 위임됐는지 검증
        verify(ownerElectionService).completeDueElection();
    }

    /**
     * 만료된 프로필 임시 이미지 정리를 파일 서비스에 위임하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delProfileDraftsDelegates() {
        // 통합 스케줄러의 프로필 임시 이미지 정리 진입점을 실행
        scheduler.delExpiredProfileDrafts();
        // 만료된 프로필 임시 이미지 정리가 파일 서비스에 위임됐는지 검증
        verify(fileService).delExpiredProfileDrafts();
    }

    /**
     * 만료된 신고 증거 정리를 신고 서비스에 위임하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delEvidenceDelegates() {
        // 통합 스케줄러의 신고 증거 정리 진입점을 실행
        scheduler.delExpiredEvidence();
        // 만료된 신고 증거 정리가 신고 서비스에 위임됐는지 검증
        verify(complaintEvidenceCleanupService).delExpiredEvidence();
    }
}
