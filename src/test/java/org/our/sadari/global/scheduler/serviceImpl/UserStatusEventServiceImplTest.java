package org.our.sadari.global.scheduler.serviceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.dto.UserStatusEventDto;
import org.our.sadari.global.scheduler.mapper.UserStatusEventMapper;
import org.our.sadari.global.scheduler.service.UserStatusEventServiceImpl;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : UserStatusEventServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 회원 상태 변경 Outbox의 Redis 반영과 재시도 조건을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성 및 이벤트 완료 검증
 */
@ExtendWith(MockitoExtension.class)
class UserStatusEventServiceImplTest {

    // 회원 상태 변경 Outbox 데이터 접근 객체
    @Mock
    private UserStatusEventMapper userStatusEventMapper;

    // 로그인 세션 회원 상태 캐시 관리 서비스
    @Mock
    private TokenRedisService tokenRedisService;

    // 스케줄러 실행 결과 기록 지원 객체
    @Mock
    private SchedulerLogSupport schedulerLogSupport;

    // 회원 상태 변경 Outbox 처리 테스트 대상
    private UserStatusEventServiceImpl userStatusEventService;

    /**
     * 한 번에 100건을 처리하는 테스트 대상 서비스를 구성한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {

        // 테스트 대역을 주입해 회원 상태 Outbox 처리 서비스를 생성한다
        userStatusEventService = new UserStatusEventServiceImpl(userStatusEventMapper, tokenRedisService, schedulerLogSupport);
        // 운영 기본값과 같은 최대 처리 건수를 테스트 대상에 설정한다
        ReflectionTestUtils.setField(userStatusEventService, "maxSize", 100);
    }

    /**
     * 현재 DB 회원 상태를 Redis에 반영하고 정지 동기화를 완료한 전달 이벤트를 삭제하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void syncStatusCompletesEvent() {

        // 정지 상태로 변경된 테스트용 Outbox 이벤트를 생성한다
        UserStatusEventDto event = createEvent(1L, 10L, Constant.USER_STAT_SUSPENDED);
        // 한 건의 처리 대상 이벤트가 조회되도록 설정한다
        when(userStatusEventMapper.getUserStatusEventList(100)).thenReturn(List.of(event));
        // 한 건 성공한 스케줄러 실행 상태를 성공으로 판정하도록 설정한다
        when(schedulerLogSupport.getSchedulerExecStatus(1, 0))
            .thenReturn(Constant.SCHEDULER_EXEC_SUCCESS);
        // 실행 이력과 종료 이력을 연결할 실행 번호를 반환하도록 설정한다
        when(schedulerLogSupport.setSchedulerLogSafely(any(SchedulerLogDto.SchedulerRunDto.class)))
            .thenReturn(1L);

        // 회원 상태 Outbox 동기화를 실행한다
        userStatusEventService.syncUserStatusEvents();

        // 처리 시점의 DB 회원 상태가 Redis에 반영됐는지 확인한다
        verify(tokenRedisService).uptUserStatus(10L, Constant.USER_STAT_SUSPENDED);
        // Redis 반영에 성공한 정지 이력이 실제 반영 완료 상태로 변경됐는지 확인한다
        verify(userStatusEventMapper).uptSuspensionSyncDone(101L, 1L);
        // 사용자 서버 처리가 끝난 임시 전달 이벤트가 삭제됐는지 확인한다
        verify(userStatusEventMapper).delUserStatusEvent(1L);
        // 처리 결과가 스케줄러 실행 이력에 반영됐는지 확인한다
        verify(schedulerLogSupport).uptSchedulerLogSafely(any(SchedulerLogDto.SchedulerRunDto.class));
    }

    /**
     * Redis 반영이 실패하면 동기화 상태와 이벤트를 변경하지 않아 다음 5분 주기에 재시도하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void syncStatusKeepsFailure() {

        // Redis 장애 재시도 조건을 검증할 테스트용 이벤트를 생성한다
        UserStatusEventDto event = createEvent(2L, 20L, Constant.USER_STAT_SUSPENDED);
        // 한 건의 처리 대상 이벤트가 조회되도록 설정한다
        when(userStatusEventMapper.getUserStatusEventList(100)).thenReturn(List.of(event));
        // Redis 상태 반영 단계에서 운영 장애가 발생하도록 설정한다
        org.mockito.Mockito.doThrow(new IllegalStateException("Redis unavailable"))
            .when(tokenRedisService).uptUserStatus(20L, Constant.USER_STAT_SUSPENDED);
        // 성공 없이 한 건 실패한 실행 상태를 실패로 판정하도록 설정한다
        when(schedulerLogSupport.getSchedulerExecStatus(0, 1))
            .thenReturn(Constant.SCHEDULER_EXEC_FAILURE);
        // 실패 상세 이력과 연결할 실행 번호를 반환하도록 설정한다
        when(schedulerLogSupport.setSchedulerLogSafely(any(SchedulerLogDto.SchedulerRunDto.class)))
            .thenReturn(2L);

        // Redis 장애가 있는 회원 상태 Outbox 동기화를 실행한다
        userStatusEventService.syncUserStatusEvents();

        // 실패한 정지 이력을 실제 반영 완료 상태로 변경하지 않았는지 확인한다
        verify(userStatusEventMapper, never()).uptSuspensionSyncDone(102L, 2L);
        // 실패한 전달 이벤트가 다음 주기에 남도록 삭제하지 않았는지 확인한다
        verify(userStatusEventMapper, never()).delUserStatusEvent(2L);
        // Redis 장애가 스케줄러 실패 상세 이력에 기록됐는지 확인한다
        verify(schedulerLogSupport).setSchedulerFailSafely(
                org.mockito.ArgumentMatchers.eq(2L)
              , org.mockito.ArgumentMatchers.eq(Constant.SCHEDULER_FAIL_EXCEPTION)
              , org.mockito.ArgumentMatchers.isNull()
              , org.mockito.ArgumentMatchers.isNull()
              , any(RuntimeException.class)
        );
    }

    /**
     * 처리할 이벤트가 없으면 Redis와 스케줄러 실행 이력을 건드리지 않는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void syncStatusSkipsEmpty() {

        // 처리할 이벤트가 없는 Outbox 조회 결과를 설정한다
        when(userStatusEventMapper.getUserStatusEventList(100)).thenReturn(Collections.emptyList());

        // 비어 있는 회원 상태 Outbox 동기화를 실행한다
        userStatusEventService.syncUserStatusEvents();

        // 처리 대상이 없을 때 Redis를 변경하지 않았는지 확인한다
        verify(tokenRedisService, never()).uptUserStatus(any(), any());
        // 빈 실행에 스케줄러 실행 이력을 만들지 않았는지 확인한다
        verify(schedulerLogSupport, never())
            .setSchedulerLogSafely(any(SchedulerLogDto.SchedulerRunDto.class));
    }

    /**
     * 테스트용 회원 상태 변경 이벤트를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param eventNumb 이벤트 번호
     * @param userNumb 회원 번호
     * @param userStat 현재 DB 회원 상태
     * @return 테스트용 Outbox 이벤트
     */
    private UserStatusEventDto createEvent(Long eventNumb, Long userNumb, String userStat) {

        // 이벤트별 테스트 값을 담을 DTO를 생성한다
        UserStatusEventDto event = new UserStatusEventDto();
        // 처리 완료 상태 변경 검증에 사용할 이벤트 번호를 설정한다
        event.setEvntNumb(eventNumb);
        // 사용자 서버가 지원하는 회원 상태 변경 유형을 설정한다
        event.setEvntType(Constant.EVENT_TYPE_USER_STATUS_CHANGED);
        // Redis 상태를 갱신할 대상 회원 번호를 설정한다
        event.setUserNumb(userNumb);
        // 처리 완료 상태를 기록할 정지 이력 번호를 설정한다
        event.setSpndNumb(eventNumb + 100L);
        // 처리 시점에 DB에서 조회된 것으로 가정할 회원 상태를 설정한다
        event.setUserStat(userStat);
        // 완성된 테스트용 Outbox 이벤트를 반환한다
        return event;
    }
}
