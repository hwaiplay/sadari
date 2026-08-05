package org.our.sadari.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : UserServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 최초 로그인 닉네임 저장과 온보딩 완료 처리를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-08-04        SeungHyeon.Kang       최초 로그인 관심분야 저장 검증 추가
 * 2026-08-05        SeungHyeon.Kang       관심분야 단일 코드 검증 추가
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
     * 관심분야 검증 실패 응답이 실제 공통 메시지를 조회할 수 있도록 테스트 메시지 소스를 초기화한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUpMessageSource() {
        // 공통 응답 메시지를 실제 프로퍼티에서 읽을 메시지 소스를 생성한다
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 서버 공통 메시지 프로퍼티를 테스트 조회 기준으로 설정한다
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 프로퍼티 파일 인코딩을 설정한다
        messageSource.setDefaultEncoding("UTF-8");
        // ResultData 실패 응답이 초기화된 공통 메시지 소스를 사용하도록 등록한다
        new MessageUtils().setMessageSource(messageSource);
    }

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

    /**
     * 활성 공통코드로 선택한 관심분야가 로그인 사용자의 목록으로 전체 교체되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptUserInterestsReplacesValidatedSelections() {
        UserDto.UserInterestDto catalogInterest = new UserDto.UserInterestDto();
        // 활성 관심분야 세부코드를 설정한다
        catalogInterest.setIntrCode("NOVEL");

        UserDto.UserInterestDto requestedInterest = new UserDto.UserInterestDto();
        // 사용자가 선택한 세부코드를 설정한다
        requestedInterest.setIntrCode("NOVEL");

        UserDto.UserInterestReqDto request = new UserDto.UserInterestReqDto();
        // 최초 로그인에서 선택한 관심분야를 요청 목록에 설정한다
        request.setInterestList(List.of(requestedInterest));

        // 선택 코드가 CATE_CODE의 활성 하위 코드에 포함되도록 조회 결과를 구성한다
        when(userMapper.getUserInterestCatalog()).thenReturn(List.of(catalogInterest));

        // 유효한 관심분야 목록으로 전체 교체를 요청한다
        ResultData result = userService.uptUserInterests(31L, request);

        // 관심분야 전체 교체가 공통 성공 코드로 응답하는지 확인한다
        assertEquals(200, result.getCode());
        // 기존 관심분야가 신규 선택 저장 전에 정리되는지 확인한다
        verify(userMapper).delUserInterests(31L);
        // 검증된 관심분야가 로그인 사용자에게 저장되는지 확인한다
        verify(userMapper).setUserInterest(31L, requestedInterest);
    }

    /**
     * CATE_CODE의 활성 하위 코드가 아닌 관심분야는 기존 선택을 삭제하지 않고 거절하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptUserInterestsRejectsInactiveInterestCode() {
        UserDto.UserInterestDto catalogInterest = new UserDto.UserInterestDto();
        // 활성 관심분야 세부코드를 설정한다
        catalogInterest.setIntrCode("NOVEL");

        UserDto.UserInterestDto requestedInterest = new UserDto.UserInterestDto();
        // 활성 허용 목록에 없는 관심분야 세부코드를 설정한다
        requestedInterest.setIntrCode("INACTIVE_CODE");

        UserDto.UserInterestReqDto request = new UserDto.UserInterestReqDto();
        // 유효하지 않은 관심분야를 교체 요청 목록에 설정한다
        request.setInterestList(List.of(requestedInterest));

        // 서버가 비교할 CATE_CODE의 활성 하위 코드 목록을 구성한다
        when(userMapper.getUserInterestCatalog()).thenReturn(List.of(catalogInterest));

        // 비활성 관심분야 코드가 포함된 전체 교체를 요청한다
        ResultData result = userService.uptUserInterests(31L, request);

        // 유효하지 않은 공통코드 요청 오류를 반환하는지 확인한다
        assertEquals(2009, result.getCode());
        // 검증 실패 전에 기존 관심분야를 삭제하지 않는지 확인한다
        verify(userMapper, never()).delUserInterests(31L);
        // 검증하지 않은 관심분야를 저장하지 않는지 확인한다
        verify(userMapper, never()).setUserInterest(31L, requestedInterest);
    }
}
