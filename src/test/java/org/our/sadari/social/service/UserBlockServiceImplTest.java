package org.our.sadari.social.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
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
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.social.dto.UserBlockDto;
import org.our.sadari.social.mapper.UserBlockMapper;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : UserBlockServiceImplTest
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 사용자 차단 등록과 양방향 격리 데이터 정리를 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 * 2026-09-14        HanWon.Jang        차단 반응 삭제 정책 반영
 */
@ExtendWith(MockitoExtension.class)
class UserBlockServiceImplTest {

    // 사용자 차단 관계 데이터 접근 대역
    @Mock
    private UserBlockMapper userBlockMapper;
    // 사용자 원본 데이터 접근 대역
    @Mock
    private UserMapper userMapper;
    // 사용자 차단 서비스 단위 테스트 대상
    private UserBlockServiceImpl userBlockService;

    /**
     * 각 테스트가 독립된 차단 서비스와 공통 메시지 소스를 사용하도록 구성
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUp() {
        // 실제 공통 실패 응답을 구성할 메시지 소스를 생성
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 서버 메시지 프로퍼티를 단위 테스트 조회 기준으로 설정
        messageSource.setBasename("messages");
        // 한글 메시지 원문을 유지하도록 테스트 인코딩을 설정
        messageSource.setDefaultEncoding("UTF-8");
        // 공통 결과 객체가 테스트 메시지 소스를 사용하도록 초기화
        new MessageUtils().setMessageSource(messageSource);
        // 사용자 차단 서비스 단위 테스트 대상을 생성
        userBlockService = new UserBlockServiceImpl(userBlockMapper, userMapper);
    }

    /**
     * 차단 등록이 모임 대기 관계와 양방향 팔로우를 같은 트랜잭션 순서로 정리하는지 검증
     *
     * @author HanWon.Jang
     */
    @Test
    void setBlockCleansRelations() {
        // 차단 대상 사용자 원본이 존재하도록 조회 결과를 설정
        when(userMapper.getUserByNumb(4L)).thenReturn(new UserDto());
        // 사용자 원본 잠금이 두 사용자를 모두 조회하도록 결과를 설정
        when(userBlockMapper.lockUsers(3L, 4L)).thenReturn(List.of(3L, 4L));

        // 3번 사용자가 4번 사용자를 차단하도록 서비스 요청을 실행
        ResultData result = userBlockService.setBlock(3L, 4L);
        // 차단 DTO에 저장된 두 사용자 번호를 확인할 인자 Capture를 생성
        ArgumentCaptor<UserBlockDto> blockCaptor = ArgumentCaptor.forClass(UserBlockDto.class);
        // 관련 데이터 정리가 정책 순서대로 호출되는지 확인할 검증 객체를 생성
        InOrder inOrder = inOrder(userBlockMapper);
        // 사용자 쌍 잠금 뒤 차단 관계가 먼저 저장되는지 검증
        inOrder.verify(userBlockMapper).lockUsers(3L, 4L);
        // 등록 요청의 차단 방향을 캡처
        inOrder.verify(userBlockMapper).setBlock(blockCaptor.capture());
        // 댓글 원본을 변경하기 전에 상대 반응 수신 알림 정리 검증
        inOrder.verify(userBlockMapper).delBlockReactionAlims(any(UserBlockDto.class));
        // 삭제 댓글에 연결된 좋아요까지 먼저 제거하는 순서 검증
        inOrder.verify(userBlockMapper).delBlockLikes(any(UserBlockDto.class));
        // 제3자 답글 구조를 유지하는 댓글 삭제 상태 전환 검증
        inOrder.verify(userBlockMapper).delBlockReplies(any(UserBlockDto.class));
        // 수락 전 직접 초대가 차단 트랜잭션에서 삭제되는지 검증
        inOrder.verify(userBlockMapper).delBlockInvitations(any(UserBlockDto.class));
        // 처리 중 가입 신청과 답변이 차단 트랜잭션에서 삭제되는지 검증
        inOrder.verify(userBlockMapper).delBlockApplications(any(UserBlockDto.class));
        // 양방향 팔로우 관계가 마지막에 삭제되는지 검증
        inOrder.verify(userBlockMapper).delBlockFollows(any(UserBlockDto.class));
        // 저장 요청이 차단 주체와 대상을 올바르게 전달하는지 검증
        assertEquals(3L, blockCaptor.getValue().getUserNumb());
        // 저장 요청의 차단 대상 사용자 번호를 검증
        assertEquals(4L, blockCaptor.getValue().getBlocNumb());
        // 모든 관계 정리가 성공 응답으로 완료되는지 검증
        assertTrue(Boolean.TRUE.equals(result.getData()));
    }

    /**
     * 차단 정리 실패가 호출자에게 전파되고 이후 정리를 실행하지 않는지 검증
     *
     * @author HanWon.Jang
     * @throws NoSuchMethodException 차단 메서드 조회 실패
     */
    @Test
    void setBlockCleanupFailure() throws NoSuchMethodException {
        // 차단 대상과 사용자 잠금은 성공하고 알림 정리에서 실패하도록 구성
        when(userMapper.getUserByNumb(4L)).thenReturn(new UserDto());
        when(userBlockMapper.lockUsers(3L, 4L)).thenReturn(List.of(3L, 4L));
        doThrow(new IllegalStateException("cleanup failed"))
                .when(userBlockMapper).delBlockReactionAlims(any(UserBlockDto.class));

        // 런타임 예외를 숨기지 않아 Spring 트랜잭션이 롤백할 수 있는지 검증
        assertThrows(IllegalStateException.class, () -> userBlockService.setBlock(3L, 4L));
        // 차단 등록 메서드의 쓰기 트랜잭션 경계가 유지되는지 검증
        assertTrue(UserBlockServiceImpl.class.getMethod("setBlock", Long.class, Long.class)
                .isAnnotationPresent(Transactional.class));
        // 실패 지점 뒤의 관계 정리가 부분 실행되지 않는지 검증
        verify(userBlockMapper, never()).delBlockLikes(any(UserBlockDto.class));
        verify(userBlockMapper, never()).delBlockReplies(any(UserBlockDto.class));
        verify(userBlockMapper, never()).delBlockFollows(any(UserBlockDto.class));
    }

    /**
     * 반대 방향 한 건만 존재해도 두 사용자의 양방향 격리 상태로 판정하는지 검증
     *
     * @author HanWon.Jang
     */
    @Test
    void isBlockedFromEitherSide() {
        // 어느 한쪽이 만든 한 건의 차단 관계가 존재하도록 조회 결과를 설정
        when(userBlockMapper.getBlockCnt(3L, 4L)).thenReturn(1);
        // 3번 사용자가 차단하지 않았더라도 반대 방향을 포함한 관계를 조회
        boolean blocked = userBlockService.isBlocked(3L, 4L);
        // 한 방향 차단으로 양쪽 사용자가 격리되는지 검증
        assertTrue(blocked);
        // 양방향 조회에 두 사용자 번호가 그대로 전달되는지 검증
        verify(userBlockMapper).getBlockCnt(3L, 4L);
    }
}
