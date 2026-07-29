package org.our.sadari.global.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.service.UserHardDeleteService;

/**
 * fileName       : LocalUserHardDeleteSchedulerTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 로컬 영구 삭제 테스트 스케줄러의 공통코드 실행 조건을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class LocalUserHardDeleteSchedulerTest {

    // 영구 삭제 대기 회원 물리 삭제 업무 서비스
    @Mock
    private UserHardDeleteService userHardDeleteService;
    // 영구 삭제 스케줄러 공통코드 활성 여부 조회 유틸리티
    @Mock
    private CodeUtil codeUtil;
    // 로컬 영구 삭제 테스트 스케줄러
    private LocalUserHardDeleteScheduler scheduler;

    /**
     * 로컬 영구 삭제 스케줄러를 Mock 의존성으로 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {

        // 로컬 스케줄러 실행 조건을 독립적으로 검증할 테스트 대상을 생성한다
        scheduler = new LocalUserHardDeleteScheduler(userHardDeleteService, codeUtil);
    }

    /**
     * USER_HARD_DELETE 상세코드가 사용 중이면 영구 삭제 서비스를 호출하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delPendingUsersForTestRunsWhenDetailCodeIsEnabled() {

        // 영구 삭제 스케줄러 상세코드가 활성 상태인 조건을 설정한다
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_USER_HARD_DELETE
        )).thenReturn(true);

        // 로컬 영구 삭제 테스트 스케줄러를 실행한다
        scheduler.delPendingUsersForTest();

        // 활성 상태에서 영구 삭제 서비스가 호출됐는지 검증한다
        verify(userHardDeleteService).delPendingUsers();
    }

    /**
     * USER_HARD_DELETE 상세코드가 중지 상태이면 영구 삭제 서비스를 호출하지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delPendingUsersForTestSkipsWhenDetailCodeIsDisabled() {

        // 영구 삭제 스케줄러 상세코드가 비활성 상태인 조건을 설정한다
        when(codeUtil.existsCode(
                Constant.CODE_SCHD_CODE
              , Constant.SCHEDULER_CODE_USER_HARD_DELETE
        )).thenReturn(false);

        // 로컬 영구 삭제 테스트 스케줄러를 실행한다
        scheduler.delPendingUsersForTest();

        // 비활성 상태에서 영구 삭제 서비스가 호출되지 않았는지 검증한다
        verify(userHardDeleteService, never()).delPendingUsers();
    }
}
