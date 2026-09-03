package org.our.sadari.social.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
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

/**
 * fileName       : UserBlockServiceImplTest
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 사용자 차단 등록과 양방향 격리 데이터 정리를 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
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
     * 각 테스트가 독립된 차단 서비스와 공통 메시지 소스를 사용하도록 구성함
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUp() {
        // 실제 공통 실패 응답을 구성할 메시지 소스를 생성함
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 서버 메시지 프로퍼티를 단위 테스트 조회 기준으로 설정함
        messageSource.setBasename("messages");
        // 한글 메시지 원문을 유지하도록 테스트 인코딩을 설정함
        messageSource.setDefaultEncoding("UTF-8");
        // 공통 결과 객체가 테스트 메시지 소스를 사용하도록 초기화함
        new MessageUtils().setMessageSource(messageSource);
        // 사용자 차단 서비스 단위 테스트 대상을 생성함
        userBlockService = new UserBlockServiceImpl(userBlockMapper, userMapper);
    }

    /**
     * 차단 등록이 모임 대기 관계와 양방향 팔로우를 같은 트랜잭션 순서로 정리하는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void setBlockCleansRelations() {
        // 차단 대상 사용자 원본이 존재하도록 조회 결과를 설정함
        when(userMapper.getUserByNumb(4L)).thenReturn(new UserDto());
        // 사용자 원본 잠금이 두 사용자를 모두 조회하도록 결과를 설정함
        when(userBlockMapper.lockUsers(3L, 4L)).thenReturn(List.of(3L, 4L));

        // 3번 사용자가 4번 사용자를 차단하도록 서비스 요청을 실행함
        ResultData result = userBlockService.setBlock(3L, 4L);
        // 차단 DTO에 저장된 두 사용자 번호를 확인할 인자 Capture를 생성함
        ArgumentCaptor<UserBlockDto> blockCaptor = ArgumentCaptor.forClass(UserBlockDto.class);
        // 관련 데이터 정리가 정책 순서대로 호출되는지 확인할 검증 객체를 생성함
        InOrder inOrder = inOrder(userBlockMapper);
        // 사용자 쌍 잠금 뒤 차단 관계가 먼저 저장되는지 검증함
        inOrder.verify(userBlockMapper).lockUsers(3L, 4L);
        // 등록 요청의 차단 방향을 캡처함
        inOrder.verify(userBlockMapper).setBlock(blockCaptor.capture());
        // 수락 전 직접 초대가 차단 트랜잭션에서 삭제되는지 검증함
        inOrder.verify(userBlockMapper).delBlockInvitations(any(UserBlockDto.class));
        // 처리 중 가입 신청과 답변이 차단 트랜잭션에서 삭제되는지 검증함
        inOrder.verify(userBlockMapper).delBlockApplications(any(UserBlockDto.class));
        // 양방향 팔로우 관계가 마지막에 삭제되는지 검증함
        inOrder.verify(userBlockMapper).delBlockFollows(any(UserBlockDto.class));
        // 저장 요청이 차단 주체와 대상을 올바르게 전달하는지 검증함
        assertEquals(3L, blockCaptor.getValue().getUserNumb());
        // 저장 요청의 차단 대상 사용자 번호를 검증함
        assertEquals(4L, blockCaptor.getValue().getBlocNumb());
        // 모든 관계 정리가 성공 응답으로 완료되는지 검증함
        assertTrue(Boolean.TRUE.equals(result.getData()));
    }

    /**
     * 반대 방향 한 건만 존재해도 두 사용자의 양방향 격리 상태로 판정하는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void isBlockedFromEitherSide() {
        // 어느 한쪽이 만든 한 건의 차단 관계가 존재하도록 조회 결과를 설정함
        when(userBlockMapper.getBlockCnt(3L, 4L)).thenReturn(1);
        // 3번 사용자가 차단하지 않았더라도 반대 방향을 포함한 관계를 조회함
        boolean blocked = userBlockService.isBlocked(3L, 4L);
        // 한 방향 차단으로 양쪽 사용자가 격리되는지 검증함
        assertTrue(blocked);
        // 양방향 조회에 두 사용자 번호가 그대로 전달되는지 검증함
        verify(userBlockMapper).getBlockCnt(3L, 4L);
    }
}
