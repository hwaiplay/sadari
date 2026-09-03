package org.our.sadari.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.scheduler.dto.UserStatusEventDto;
import org.our.sadari.global.scheduler.mapper.UserStatusEventMapper;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserSuspensionDto;
import org.our.sadari.user.mapper.UserSuspensionMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * fileName       : UserSuspensionServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 기간 정지 만료와 우선 상태 보존을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성 및 정지 상태 검증
 */
@ExtendWith(MockitoExtension.class)
class UserSuspensionServiceImplTest {

    // 회원 정지 이력 Mapper 대역
    @Mock
    private UserSuspensionMapper userSuspensionMapper;

    // 로그인 세션 상태 서비스 대역
    @Mock
    private TokenRedisService tokenRedisService;

    // 회원 상태 변경 Outbox Mapper 대역
    @Mock
    private UserStatusEventMapper userStatusEventMapper;

    // 회원 정지 업무 검증 대상
    @InjectMocks
    private UserSuspensionServiceImpl userSuspensionService;

    /**
     * 활성 정지 이력이 없고 DB가 정상 상태이면 남은 Redis 정지 상태를 보정하는지 확인함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void missingSuspRestoresRedis() {
        when(userSuspensionMapper.getLatestActiveSuspension(7L)).thenReturn(null);
        when(userSuspensionMapper.getUserStatus(7L)).thenReturn("ACTIVE");

        assertNotNull(userSuspensionService.getUserSuspension(7L));
        verify(tokenRedisService).uptUserStatus(7L, "ACTIVE");
    }

    /**
     * DB가 여전히 정지 상태이면 활성 이력이 없어도 접근 제한을 임의로 해제하지 않는지 확인함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void missingSuspKeepsRedis() {
        when(userSuspensionMapper.getLatestActiveSuspension(7L)).thenReturn(null);
        when(userSuspensionMapper.getUserStatus(7L)).thenReturn("SUSPENDED");

        assertNotNull(userSuspensionService.getUserSuspension(7L));
        verify(tokenRedisService, never()).uptUserStatus(any(Long.class), any(String.class));
    }

    /**
     * 정지 만료 시 DB 회원 상태가 실제 복구된 경우에만 Redis 상태도 복구하는지 확인함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void expiredSuspRestoresRedis() {
        UserSuspensionDto suspension = createExpiredSuspension();
        when(userSuspensionMapper.getLatestActiveSuspension(7L)).thenReturn(suspension);
        when(userSuspensionMapper.uptSuspensionExpired(31L)).thenReturn(1);
        when(userSuspensionMapper.uptUserStatusAfterSuspend(7L, "ACTIVE")).thenReturn(1);
        when(userSuspensionMapper.uptSuspensionSyncPending(31L)).thenReturn(1);
        when(userStatusEventMapper.setUserStatusEvent(any(UserStatusEventDto.class))).thenReturn(1);

        assertTrue(userSuspensionService.uptExpiredSuspension(7L));
        verify(userSuspensionMapper).uptSuspensionSyncPending(31L);
        verify(userStatusEventMapper).setUserStatusEvent(any(UserStatusEventDto.class));
        verify(tokenRedisService).uptUserStatus(7L, "ACTIVE");
    }

    /**
     * 영구 삭제 대기 같은 우선 상태가 남으면 정지 만료가 Redis 상태를 덮지 않는지 확인함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void expiredSuspKeepsPriority() {
        UserSuspensionDto suspension = createExpiredSuspension();
        when(userSuspensionMapper.getLatestActiveSuspension(7L)).thenReturn(suspension);
        when(userSuspensionMapper.uptSuspensionExpired(31L)).thenReturn(1);
        when(userSuspensionMapper.uptUserStatusAfterSuspend(7L, "ACTIVE")).thenReturn(0);

        assertTrue(userSuspensionService.uptExpiredSuspension(7L));
        verify(userSuspensionMapper, never()).uptSuspensionSyncPending(31L);
        verify(userStatusEventMapper, never()).setUserStatusEvent(any(UserStatusEventDto.class));
        verify(tokenRedisService, never()).uptUserStatus(7L, "ACTIVE");
    }

    /**
     * 종료 시각이 지난 기간 정지 테스트 객체를 생성함
     *
     * @author SeungHyeon.Kang
     * @return 만료 대상 기간 정지
     */
    private UserSuspensionDto createExpiredSuspension() {
        UserSuspensionDto suspension = new UserSuspensionDto();
        suspension.setSpndNumb(31L);
        suspension.setSpndType("PERIOD");
        suspension.setPrevStat("ACTIVE");
        suspension.setEndxDate(LocalDateTime.now().minusMinutes(1));
        return suspension;
    }
}
