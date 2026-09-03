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
import org.our.sadari.feed.mapper.FeedMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockMultipartFile;

/**
 * fileName       : UserServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 최초 로그인 닉네임 저장과 온보딩 완료 처리를 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-08-04        SeungHyeon.Kang       최초 로그인 관심분야 저장 검증 추가
 * 2026-08-05        SeungHyeon.Kang       관심분야 단일 코드 검증 추가
 * 2026-08-06        SeungHyeon.Kang    프로필과 배경 이미지 교체 파일 정리 검증 추가
 * 2026-08-07        SeungHyeon.Kang    닉네임 공백 금지 검증 추가
 * 2026-08-19        SeungHyeon.Kang    공통 닉네임 검증 경로 회귀 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // 사용자 데이터 접근 객체 대역
    @Mock
    private UserMapper userMapper;
    // 피드 데이터 접근 객체 대역
    @Mock
    private FeedMapper feedMapper;
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
     * 관심분야 검증 실패 응답이 실제 공통 메시지를 조회할 수 있도록 테스트 메시지 소스를 초기화함
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUpMessageSource() {
        // 공통 응답 메시지를 실제 프로퍼티에서 읽을 메시지 소스를 생성함
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 서버 공통 메시지 프로퍼티를 테스트 조회 기준으로 설정함
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 프로퍼티 파일 인코딩을 설정함
        messageSource.setDefaultEncoding("UTF-8");
        // ResultData 실패 응답이 초기화된 공통 메시지 소스를 사용하도록 등록함
        new MessageUtils().setMessageSource(messageSource);
    }

    /**
     * 유효한 닉네임 저장 시 사용자 행과 Redis 닉네임을 갱신하고 완료 프로필을 반환하는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptOnboardCompletes() {

        UserDto request = new UserDto();
        // 온보딩에서 확정할 사용자 닉네임을 요청 DTO에 설정함
        request.setUserNick("차분한독서가");

        UserDto savedUser = new UserDto();
        // 저장 후 조회될 사용자 번호를 설정함
        savedUser.setUserNumb(31L);
        // 저장 후 조회될 닉네임을 설정함
        savedUser.setUserNick("차분한독서가");
        // 온보딩 완료 상태를 설정함
        savedUser.setOnbdYsno("Y");

        // 유효한 닉네임에는 비속어가 없도록 탐지 결과를 구성함
        when(badWordDetectionService.findBadWord("차분한독서가")).thenReturn(Optional.empty());
        // 다른 회원과 닉네임이 중복되지 않도록 조회 결과를 구성함
        when(userMapper.getUserNickDuplicateCnt(request)).thenReturn(0);
        // 닉네임과 온보딩 완료 UPDATE가 한 행에 반영되도록 결과를 구성함
        when(userMapper.uptUserOnboarding(request)).thenReturn(1);
        // 완료 응답에서 최신 프로필을 반환하도록 사용자 조회 결과를 구성함
        when(userMapper.getUserByNumb(31L)).thenReturn(savedUser);

        // 유효한 닉네임으로 최초 로그인 온보딩 완료를 요청함
        ResultData result = userService.uptOnboarding(31L, request);

        // 온보딩 완료 API가 공통 성공 코드로 응답하는지 확인함
        assertEquals(200, result.getCode());
        // 완료 응답에 저장한 닉네임이 포함되는지 확인함
        assertEquals("차분한독서가", ((Map<?, ?>) result.getData()).get("userNick"));
        // 완료 응답에 온보딩 완료 상태가 포함되는지 확인함
        assertEquals("Y", ((Map<?, ?>) result.getData()).get("onbdYsno"));
        // DB 커밋 후처리 경로에서 로그인 세션의 닉네임을 같은 값으로 갱신하는지 확인함
        verify(tokenRedisService).uptUserNick(31L, "차분한독서가");
    }

    /**
     * 공백이 포함된 닉네임은 온보딩 완료와 저장 처리 전에 거절하는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptOnboardRejectsSpace() {
        // 공백 포함 닉네임 검증에 사용할 요청 DTO를 생성함
        UserDto request = new UserDto();
        // 저장할 수 없는 공백 포함 닉네임을 요청 DTO에 설정함
        request.setUserNick("차분한 독서가");

        // 공백 포함 닉네임으로 최초 로그인 온보딩 완료를 요청함
        ResultData result = userService.uptOnboarding(31L, request);

        // 잘못된 닉네임 형식에 대응하는 공통 요청 오류인지 확인함
        assertEquals(2009, result.getCode());
        // 형식 검증을 통과하지 못한 닉네임은 중복 조회에 사용하지 않는지 확인함
        verify(userMapper, never()).getUserNickDuplicateCnt(request);
        // 형식 검증을 통과하지 못한 닉네임을 저장하지 않는지 확인함
        verify(userMapper, never()).uptUserOnboarding(request);
    }

    /**
     * 다른 회원이 사용 중인 닉네임은 온보딩 완료 상태를 변경하지 않고 거절하는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptOnboardRejectsDup() {
        // 닉네임 중복 검증에 사용할 온보딩 요청을 생성함
        UserDto request = new UserDto();
        // 다른 회원이 사용 중인 닉네임을 요청 DTO에 설정함
        request.setUserNick("차분한독서가");

        // 중복 검증까지 진행되도록 닉네임 비속어 검사 결과를 구성함
        when(badWordDetectionService.findBadWord("차분한독서가")).thenReturn(Optional.empty());
        // 다른 회원이 같은 닉네임을 사용하는 조회 결과를 구성함
        when(userMapper.getUserNickDuplicateCnt(request)).thenReturn(1);

        // 중복된 닉네임으로 최초 로그인 온보딩 완료를 요청함
        ResultData result = userService.uptOnboarding(31L, request);

        // 공통 닉네임 중복 오류를 반환하는지 확인함
        assertEquals(ResultEnum.USER_NICK_DUPLICATED.getCode(), result.getCode());
        // 중복 닉네임으로 온보딩 완료 상태를 저장하지 않는지 확인함
        verify(userMapper, never()).uptUserOnboarding(request);
    }

    /**
     * 활성 공통코드로 선택한 관심분야가 로그인 사용자의 목록으로 전체 교체되는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptInterestsReplacesAll() {
        UserDto.UserInterestDto catalogInterest = new UserDto.UserInterestDto();
        // 활성 관심분야 세부코드를 설정함
        catalogInterest.setIntrCode("NOVEL");

        UserDto.UserInterestDto requestedInterest = new UserDto.UserInterestDto();
        // 사용자가 선택한 세부코드를 설정함
        requestedInterest.setIntrCode("NOVEL");

        UserDto.UserInterestReqDto request = new UserDto.UserInterestReqDto();
        // 최초 로그인에서 선택한 관심분야를 요청 목록에 설정함
        request.setInterestList(List.of(requestedInterest));

        // 선택 코드가 CATE_CODE의 활성 하위 코드에 포함되도록 조회 결과를 구성함
        when(userMapper.getUserInterestCatalog()).thenReturn(List.of(catalogInterest));

        // 유효한 관심분야 목록으로 전체 교체를 요청함
        ResultData result = userService.uptUserInterests(31L, request);

        // 관심분야 전체 교체가 공통 성공 코드로 응답하는지 확인함
        assertEquals(200, result.getCode());
        // 기존 관심분야가 신규 선택 저장 전에 정리되는지 확인함
        verify(userMapper).delUserInterests(31L);
        // 검증된 관심분야가 로그인 사용자에게 저장되는지 확인함
        verify(userMapper).setUserInterest(31L, requestedInterest);
    }

    /**
     * CATE_CODE의 활성 하위 코드가 아닌 관심분야는 기존 선택을 삭제하지 않고 거절하는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptRejectsInactiveCode() {
        UserDto.UserInterestDto catalogInterest = new UserDto.UserInterestDto();
        // 활성 관심분야 세부코드를 설정함
        catalogInterest.setIntrCode("NOVEL");

        UserDto.UserInterestDto requestedInterest = new UserDto.UserInterestDto();
        // 활성 허용 목록에 없는 관심분야 세부코드를 설정함
        requestedInterest.setIntrCode("INACTIVE_CODE");

        UserDto.UserInterestReqDto request = new UserDto.UserInterestReqDto();
        // 유효하지 않은 관심분야를 교체 요청 목록에 설정함
        request.setInterestList(List.of(requestedInterest));

        // 서버가 비교할 CATE_CODE의 활성 하위 코드 목록을 구성함
        when(userMapper.getUserInterestCatalog()).thenReturn(List.of(catalogInterest));

        // 비활성 관심분야 코드가 포함된 전체 교체를 요청함
        ResultData result = userService.uptUserInterests(31L, request);

        // 유효하지 않은 공통코드 요청 오류를 반환하는지 확인함
        assertEquals(2009, result.getCode());
        // 검증 실패 전에 기존 관심분야를 삭제하지 않는지 확인함
        verify(userMapper, never()).delUserInterests(31L);
        // 검증하지 않은 관심분야를 저장하지 않는지 확인함
        verify(userMapper, never()).setUserInterest(31L, requestedInterest);
    }

    /**
     * 프로필과 배경 이미지를 교체하면 사용자 참조 변경 뒤 기존 파일 정리를 요청하는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws Exception 파일 저장 서비스 모의 호출에서 오류가 발생한 경우
     */
    @Test
    void uptMeDeletesOldImages() throws Exception {
        // 프로필 수정 요청 DTO를 생성함
        UserDto request = new UserDto();
        // 수정할 닉네임을 설정함
        request.setUserNick("차분한독서가");
        // 수정할 한줄소개를 설정함
        request.setIntrCntn("책과 함께 쉬어갑니다");

        // 교체 전 파일 번호를 담을 현재 사용자 정보를 생성함
        UserDto currentUser = new UserDto();
        // 교체 전 프로필 파일 번호를 설정함
        currentUser.setProfNumb(10L);
        // 교체 전 배경 파일 번호를 설정함
        currentUser.setBgimNumb(20L);

        // 수정 완료 응답에 사용할 최신 사용자 정보를 생성함
        UserDto updatedUser = new UserDto();
        // 수정된 사용자 번호를 설정함
        updatedUser.setUserNumb(31L);
        // 수정된 사용자 닉네임을 설정함
        updatedUser.setUserNick("차분한독서가");

        // 파일 교체 요청에 사용할 프로필 이미지 파일을 생성함
        MockMultipartFile profileImage = new MockMultipartFile("profileImage", "profile.png", "image/png", new byte[] {1});
        // 파일 교체 요청에 사용할 배경 이미지 파일을 생성함
        MockMultipartFile backgroundImage = new MockMultipartFile("backgroundImage", "background.png", "image/png", new byte[] {2});

        // 닉네임과 한줄소개가 비속어 검사를 통과하도록 결과를 설정함
        when(badWordDetectionService.findBadWord("차분한독서가")).thenReturn(Optional.empty());
        // 한줄소개 비속어 검사가 정상 통과하도록 결과를 설정함
        when(badWordDetectionService.findBadWord("책과 함께 쉬어갑니다")).thenReturn(Optional.empty());
        // 다른 사용자와 닉네임이 중복되지 않도록 조회 결과를 설정함
        when(userMapper.getUserNickDuplicateCnt(request)).thenReturn(0);
        // 동시 수정 잠금 조회에서 교체 전 파일 번호를 반환하도록 설정함
        when(userMapper.getUserFileForUpdate(31L)).thenReturn(currentUser);
        // 신규 프로필 파일 번호를 반환하도록 설정함
        when(fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, 31L)).thenReturn(11L);
        // 신규 배경 파일 번호를 반환하도록 설정함
        when(fileService.setUploadedImage(backgroundImage, Constant.FILE_TYPE_BACKGROUND, 31L)).thenReturn(21L);
        // 사용자 프로필 UPDATE가 정상 반영되도록 결과를 설정함
        when(userMapper.uptUserProfile(request)).thenReturn(1);
        // 수정 완료 응답에서 최신 사용자 정보를 반환하도록 설정함
        when(userMapper.getUserByNumb(31L)).thenReturn(updatedUser);

        // 프로필과 배경 이미지가 포함된 사용자 프로필 수정을 요청함
        ResultData result = userService.uptMe(31L, request, profileImage, backgroundImage);

        // 프로필 수정이 공통 성공 코드로 응답하는지 확인함
        assertEquals(200, result.getCode());
        // 사용자 참조에서 교체된 이전 프로필 파일 정리를 요청하는지 확인함
        verify(fileService).delFile(10L);
        // 사용자 참조에서 교체된 이전 배경 파일 정리를 요청하는지 확인함
        verify(fileService).delFile(20L);
    }

    /**
     * 프로필 한줄소개에 비속어가 있으면 사용자와 파일을 변경하지 않고 거절하는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptMeRejectsIntroBadWord() {
        // 한줄소개 비속어 검증에 사용할 프로필 수정 요청을 생성함
        UserDto request = new UserDto();
        // 형식 검증을 통과할 닉네임을 요청 DTO에 설정함
        request.setUserNick("차분한독서가");
        // 저장을 차단할 비속어가 포함된 한줄소개를 요청 DTO에 설정함
        request.setIntrCntn("금지표현");

        // 한줄소개 검사까지 진행되도록 닉네임 비속어 검사 결과를 구성함
        when(badWordDetectionService.findBadWord("차분한독서가")).thenReturn(Optional.empty());
        // 한줄소개에서 탐지할 비속어를 반환하도록 결과를 구성함
        when(badWordDetectionService.findBadWord("금지표현")).thenReturn(Optional.of("금지표현"));

        // 비속어가 포함된 한줄소개로 프로필 수정을 요청함
        ResultData result = userService.uptMe(31L, request, null, null);

        // 공통 비속어 포함 오류를 반환하는지 확인함
        assertEquals(ResultEnum.COMMON_BAD_WORD_INCLUDED.getCode(), result.getCode());
        // 비속어 검증에 실패한 프로필 정보를 저장하지 않는지 확인함
        verify(userMapper, never()).uptUserProfile(request);
        // 비속어 검증에 실패한 요청은 현재 파일 정보를 잠금 조회하지 않는지 확인함
        verify(userMapper, never()).getUserFileForUpdate(31L);
    }
}
