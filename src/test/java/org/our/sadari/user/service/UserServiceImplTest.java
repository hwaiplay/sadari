package org.our.sadari.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;

/**
 * fileName       : UserServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 최초 로그인 닉네임 저장과 온보딩 완료 처리를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // 사용자 데이터 접근 객체 대역
    @Mock
    private UserMapper userMapper;
    // 파일 업무 서비스 대역
    @Mock
    private FileService fileService;
    // 비속어 탐지 서비스 대역
    @Mock
    private BadWordDetectionService badWordDetectionService;
    // 로그인 사용자 Redis 서비스 대역
    @Mock
    private TokenRedisService tokenRedisService;
    // 온보딩 업무 검증 대상 서비스
    @InjectMocks
    private UserServiceImpl userService;

    /**
     * 유효한 닉네임 저장 시 사용자 행과 Redis 닉네임을 갱신하고 완료 프로필을 반환하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptOnboardingUpdatesNicknameAndCompletionState() {

        UserDto request = new UserDto();
        // 온보딩에서 확정할 사용자 닉네임을 요청 DTO에 설정한다
        request.setUserNick("차분한 독서가");

        UserDto savedUser = new UserDto();
        // 저장 후 조회될 사용자 번호를 설정한다
        savedUser.setUserNumb(31L);
        // 저장 후 조회될 닉네임을 설정한다
        savedUser.setUserNick("차분한 독서가");
        // 온보딩 완료 상태를 설정한다
        savedUser.setOnbdYsno("Y");

        // 유효한 닉네임에는 비속어가 없도록 탐지 결과를 구성한다
        when(badWordDetectionService.findBadWord("차분한 독서가")).thenReturn(Optional.empty());
        // 다른 회원과 닉네임이 중복되지 않도록 조회 결과를 구성한다
        when(userMapper.getUserNickDuplicateCnt(request)).thenReturn(0);
        // 닉네임과 온보딩 완료 UPDATE가 한 행에 반영되도록 결과를 구성한다
        when(userMapper.uptUserOnboarding(request)).thenReturn(1);
        // 완료 응답에서 최신 프로필을 반환하도록 사용자 조회 결과를 구성한다
        when(userMapper.getUserByNumb(31L)).thenReturn(savedUser);

        // 유효한 닉네임으로 최초 로그인 온보딩 완료를 요청한다
        ResultData result = userService.uptOnboarding(31L, request);

        // 온보딩 완료 API가 공통 성공 코드로 응답하는지 확인한다
        assertEquals(200, result.getCode());
        // 완료 응답에 저장한 닉네임이 포함되는지 확인한다
        assertEquals("차분한 독서가", ((Map<?, ?>) result.getData()).get("userNick"));
        // 완료 응답에 온보딩 완료 상태가 포함되는지 확인한다
        assertEquals("Y", ((Map<?, ?>) result.getData()).get("onbdYsno"));
        // DB 커밋 후처리 경로에서 로그인 세션의 닉네임을 같은 값으로 갱신하는지 확인한다
        verify(tokenRedisService).uptUserNick(31L, "차분한 독서가");
    }
}
