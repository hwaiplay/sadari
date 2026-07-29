package org.our.sadari.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.service.UserHardDeleteService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * fileName       : LocalUserHardDeleteScheduler
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 로컬 환경에서 영구 삭제 대기 회원을 짧은 주기로 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("loc")
@ConditionalOnProperty(prefix = "withdrawal", name = "hard-delete-test-enabled", havingValue = "true")
public class LocalUserHardDeleteScheduler {

    // 영구 삭제 대기 회원 물리 삭제 업무 서비스
    private final UserHardDeleteService userHardDeleteService;
    // 영구 삭제 스케줄러 공통코드 활성 여부 조회 유틸리티
    private final CodeUtil codeUtil;

    /**
     * 로컬 탈퇴 테스트를 위해 10초마다 삭제 예정일이 지난 회원을 영구 삭제한다.
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "*/10 * * * * *")
    public void delPendingUsersForTest() {

        // 운영 스케줄러와 동일하게 공통코드가 중지 상태이면 삭제 업무를 실행하지 않는다
        if (!codeUtil.existsCode(Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_HARD_DELETE)) {
            // 로컬 테스트 스케줄러 중지 상태를 디버그 로그에 기록한다
            log.debug("로컬 회원 영구 삭제 스케줄러가 사용 중지 상태여서 실행하지 않습니다. 공통코드={}, 상세코드={}", Constant.CODE_SCHD_CODE, Constant.SCHEDULER_CODE_USER_HARD_DELETE);
            // 사용 중지된 로컬 테스트 스케줄러 처리를 종료한다
            return;
        }

        // 삭제 예정일이 지난 로컬 테스트 회원의 연관 데이터와 회원 원본을 삭제한다
        userHardDeleteService.delPendingUsers();
    }
}
