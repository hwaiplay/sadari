package org.our.sadari.user.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.service.UserIdEncryptionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.auth.dto.KakaoAccountDto;
import org.our.sadari.user.auth.dto.KakaoTokenDto;
import org.our.sadari.user.auth.provider.KakaoAuthProvider;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.LoginHistoryMapper;
import org.our.sadari.user.mapper.UserMapper;
import org.our.sadari.user.mapper.UserWithdrawalMapper;
import org.our.sadari.user.service.NicknameGenerationService;
import org.our.sadari.user.service.UserSuspensionService;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * fileName       : AuthServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : Kakao 로그인 시 비활성화 계정 재활성화와 안내 상태 전달을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성 및 로그인 제공자 검증
 * 2026-08-13        SeungHyeon.Kang    재가입 차단·OAuth 예외 검증
 * 2026-08-16        SeungHyeon.Kang    테스트 간 Locale 변경에도 공통 인증 메시지 검증 안정화
 * 2026-08-22        SeungHyeon.Kang    Kakao 기본 프로필 제외 검증
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    // Kakao 인증 연동 제공 객체 대역
    @Mock
    private KakaoAuthProvider kakaoAuthProvider;
    // JWT 발급 제공 객체 대역
    @Mock
    private JwtProvider jwtProvider;
    // 로그인 세션 Redis 서비스 대역
    @Mock
    private TokenRedisService tokenRedisService;
    // 로그인 이력 데이터 접근 객체 대역
    @Mock
    private LoginHistoryMapper loginHistoryMapper;
    // 사용자 데이터 접근 객체 대역
    @Mock
    private UserMapper userMapper;
    // 프로필 파일 서비스 대역
    @Mock
    private FileService fileService;
    // OAuth 식별값 암호화 서비스 대역
    @Mock
    private UserIdEncryptionService userIdEncryptionService;
    // 신규 회원 닉네임 발급 서비스 대역
    @Mock
    private NicknameGenerationService nicknameGenerationService;
    // 회원 정지 만료 처리 서비스 대역
    @Mock
    private UserSuspensionService userSuspensionService;
    // 탈퇴 계정의 유효 제재 조회 대역
    @Mock
    private UserWithdrawalMapper userWithdrawalMapper;
    // Kakao 로그인과 계정 재활성화 검증 대상
    @InjectMocks
    private AuthServiceImpl authService;

    /**
     * 실패 응답 메시지 조회에 사용할 테스트 메시지 소스를 초기화함
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUpMessageSource() {
        // 탈퇴 후 제재가 남은 계정의 실패 응답 메시지를 테스트 환경에 등록함
        StaticMessageSource messageSource = new StaticMessageSource();
        // 인증 차단 결과 코드가 사용자 메시지로 변환되도록 키를 등록함
        messageSource.addMessage("auth.withdrawn.suspended", java.util.Locale.KOREAN, "가입할 수 없는 계정입니다.");
        // 테스트 실행 JVM 기본 언어에서도 같은 차단 메시지를 조회하도록 등록함
        messageSource.addMessage("auth.withdrawn.suspended", java.util.Locale.getDefault(), "가입할 수 없는 계정입니다.");
        // 예외 경로가 발생해도 원래 실패 원인을 가리지 않도록 공통 인증 메시지를 등록함
        messageSource.addMessage("auth.common.fail", java.util.Locale.KOREAN, "인증에 실패했습니다.");
        // 테스트 실행 JVM 기본 언어에서도 공통 인증 메시지를 조회하도록 등록함
        messageSource.addMessage("auth.common.fail", java.util.Locale.getDefault(), "인증에 실패했습니다.");
        // 공통 결과 객체가 테스트 메시지 소스를 사용하도록 초기화함
        new MessageUtils().setMessageSource(messageSource);
    }

    /**
     * 로그인 제공자 코드가 축약형 없이 풀네임으로 정의되는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void loginCodesUseFullNames() {
        // Kakao 로그인과 사용자 및 로그인 이력 저장에 동일한 풀네임 코드가 사용되는지 검증함
        assertEquals("KAKAO", AuthConstant.PROV_KAKAO);
        // Naver 제공자 확장 시에도 축약형이 다시 저장되지 않도록 풀네임 계약을 검증함
        assertEquals("NAVER", AuthConstant.PROV_NAVER);
        // Google 제공자 확장 시에도 축약형이 다시 저장되지 않도록 풀네임 계약을 검증함
        assertEquals("GOOGLE", AuthConstant.PROV_GOOGLE);
    }

    /**
     * Kakao가 인가 코드 교환을 거절하면 HTTP 예외 대신 공통 인증 실패를 반환하는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws Exception Kakao 인증 응답 대역 구성 중 발생
     */
    @Test
    void kakaoLoginHandlesKakao4xx() throws Exception {
        // 만료되거나 재사용된 인가 코드에 대한 Kakao 400 응답 예외를 생성함
        HttpClientErrorException kakaoException = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8
        );

        // 토큰 교환 단계에서 Kakao 인증 거절 예외가 발생하도록 구성함
        when(kakaoAuthProvider.getKakaoToken("expired-authorization-code")).thenThrow(kakaoException);

        // 거절된 Kakao 인가 코드로 로그인을 요청함
        ResultData result = authService.kakaoLogin("expired-authorization-code", "127.0.0.1", "test-agent");

        // 원시 HTTP 예외 대신 공통 인증 실패 코드가 반환되는지 검증함
        assertEquals(1001, result.getCode());
        // 토큰 교환 실패 뒤 Kakao 사용자 정보 조회를 시작하지 않는지 검증함
        verify(kakaoAuthProvider, never()).getKakaoAccount(any(KakaoTokenDto.class));
        // 외부 인증이 완료되지 않은 상태에서 회원 조회를 시작하지 않는지 검증함
        verify(userMapper, never()).getUserByIdxx(anyString());
    }

    /**
     * 비활성화 회원이 같은 Kakao 계정으로 로그인하면 정상 상태로 복구하고 복귀 안내 여부를 반환하는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws Exception Kakao 인증 응답 대역 구성 중 발생
     */
    @Test
    void kakaoLoginReactivates() throws Exception {
        // Kakao 토큰 교환 결과를 로그인 서비스에 제공할 객체를 생성함
        KakaoTokenDto kakaoToken = new KakaoTokenDto();
        // Kakao 프로필 이미지가 포함된 계정 응답을 생성함
        KakaoAccountDto kakaoAccount = getKakaoAccount();

        UserDto savedUser = new UserDto();
        // 비활성화된 기존 회원 번호를 설정함
        savedUser.setUserNumb(31L);
        // 기존 회원 권한을 설정함
        savedUser.setUserRole(AuthConstant.ROLE_USER);
        // 기존 회원 닉네임을 설정함
        savedUser.setUserNick("돌아온 독서가");
        // 재로그인 전 비활성화 상태를 설정함
        savedUser.setUserStat(Constant.USER_STAT_WITHDRAWN);
        // 기존 프로필 파일이 유지되도록 파일 번호를 설정함
        savedUser.setProfNumb(7L);
        // 비활성화 요청일을 설정함
        savedUser.setWthdDate(LocalDateTime.now());

        // Kakao 인가 코드로 토큰을 교환하도록 결과를 구성함
        when(kakaoAuthProvider.getKakaoToken("authorization-code")).thenReturn(kakaoToken);
        // Kakao 토큰으로 동일 계정의 프로필을 조회하도록 결과를 구성함
        when(kakaoAuthProvider.getKakaoAccount(kakaoToken)).thenReturn(kakaoAccount);
        // Kakao 식별값을 DB 조회용 결정적 암호문으로 변환하도록 결과를 구성함
        when(userIdEncryptionService.encryptForStorage("12345")).thenReturn("encrypted-provider-id");
        // 암호화한 Kakao 식별값으로 비활성화 회원이 조회되도록 결과를 구성함
        when(userMapper.getUserByIdxx("encrypted-provider-id")).thenReturn(savedUser);
        // 재활성화된 회원에게 Access Token을 발급하도록 결과를 구성함
        when(jwtProvider.createAccessToken(eq(31L), eq(AuthConstant.ROLE_USER), anyString())).thenReturn("access-token");
        // 재활성화된 회원에게 Refresh Token을 발급하도록 결과를 구성함
        when(jwtProvider.createRefreshToken(eq(31L), anyString())).thenReturn("refresh-token");
        // Redis 로그인 세션의 유지 시간을 설정하도록 결과를 구성함
        when(jwtProvider.getRefreshTokenValidSec()).thenReturn(3600L);

        // 같은 Kakao 계정으로 비활성화 회원의 재로그인을 요청함
        ResultData result = authService.kakaoLogin("authorization-code", "127.0.0.1", "test-agent");
        // 로그인 성공 응답에 포함된 토큰과 복귀 상태를 조회함
        TokenDto token = (TokenDto) result.getData();

        // 비활성화 계정 재로그인이 정상 성공 코드로 처리되는지 검증함
        assertEquals(200, result.getCode());
        // OAuth 완료 화면이 복귀 정책을 안내할 수 있도록 재활성화 여부가 전달되는지 검증함
        assertTrue(token.isAccountReactivated());
        // 기존 회원 상태가 정상 이용 상태로 변경되는지 검증함
        assertEquals(Constant.USER_STAT_ACTIVE, savedUser.getUserStat());
        // 비활성화 요청일이 재활성화 시 제거되는지 검증함
        assertNull(savedUser.getWthdDate());
        // 변경된 사용자 상태가 DB에 저장되는지 검증함
        verify(userMapper).uptUserStatus(savedUser);
        // Redis 로그인 상태에도 재활성화된 회원 상태가 저장되는지 검증함
        verify(tokenRedisService).setLoginUserInfo(
                eq(31L)
              , anyString()
              , eq("refresh-token")
              , eq("돌아온 독서가")
              , eq(Constant.USER_STAT_ACTIVE)
              , eq(3600L)
        );
    }

    /**
     * 물리 삭제된 과거 회원 번호에 유효 제재가 남아 있으면 새 회원을 만들지 않는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws Exception Kakao 인증 응답 대역 구성 중 발생
     */
    @Test
    void blocksSuspendedWithdrawal() throws Exception {
        // Kakao 토큰 교환 결과를 로그인 서비스에 제공할 객체를 생성함
        KakaoTokenDto kakaoToken = new KakaoTokenDto();
        // 물리 삭제된 계정과 같은 식별값의 Kakao 응답을 생성함
        KakaoAccountDto kakaoAccount = getKakaoAccount();

        // Kakao 인가 코드로 토큰을 교환하도록 결과를 구성함
        when(kakaoAuthProvider.getKakaoToken("authorization-code")).thenReturn(kakaoToken);
        // 토큰으로 물리 삭제된 계정의 Kakao 프로필을 조회하도록 결과를 구성함
        when(kakaoAuthProvider.getKakaoAccount(kakaoToken)).thenReturn(kakaoAccount);
        // 현재 회원 테이블 조회용 결정적 암호문을 생성하도록 결과를 구성함
        when(userIdEncryptionService.encryptForStorage("12345")).thenReturn("encrypted-provider-id");
        // 물리 삭제가 완료되어 현재 회원 행은 조회되지 않도록 구성함
        when(userMapper.getUserByIdxx("encrypted-provider-id")).thenReturn(null);
        // 탈퇴 이력 비교용 식별값 해시를 생성하도록 결과를 구성함
        when(userIdEncryptionService.hashForAudit("12345")).thenReturn("hashed-provider-id");
        // 같은 해시에 연결된 과거 회원 번호의 유효 제재가 조회되도록 구성함
        when(userWithdrawalMapper.getActiveSuspCntByIdHash("hashed-provider-id")).thenReturn(1);

        // 제재가 남은 물리 삭제 계정으로 Kakao 로그인을 요청함
        ResultData result = authService.kakaoLogin("authorization-code", "127.0.0.1", "test-agent");

        // 관리자 해제 전 재가입 전용 차단 코드가 반환되는지 검증함
        assertEquals(1005, result.getCode());
        // 차단된 Kakao 계정에 새 회원 번호가 발급되지 않는지 검증함
        verify(userMapper, never()).setUser(any(UserDto.class));
    }

    /**
     * 과거 회원 번호의 제재가 모두 해제되면 같은 Kakao 계정에 새 회원 번호를 발급하는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws Exception Kakao 인증 응답 대역 구성 중 발생
     */
    @Test
    void createsUserAfterRelease() throws Exception {
        // Kakao 토큰 교환 결과를 로그인 서비스에 제공할 객체를 생성함
        KakaoTokenDto kakaoToken = new KakaoTokenDto();
        // 제재가 해제된 탈퇴 계정과 같은 식별값의 Kakao 응답을 생성함
        KakaoAccountDto kakaoAccount = getKakaoAccount();

        // Kakao 인가 코드로 토큰을 교환하도록 결과를 구성함
        when(kakaoAuthProvider.getKakaoToken("authorization-code")).thenReturn(kakaoToken);
        // 토큰으로 제재가 해제된 계정의 Kakao 프로필을 조회하도록 결과를 구성함
        when(kakaoAuthProvider.getKakaoAccount(kakaoToken)).thenReturn(kakaoAccount);
        // 현재 회원 테이블 조회용 결정적 암호문을 생성하도록 결과를 구성함
        when(userIdEncryptionService.encryptForStorage("12345")).thenReturn("encrypted-provider-id");
        // 이전 회원이 물리 삭제되어 현재 회원 행은 조회되지 않도록 구성함
        when(userMapper.getUserByIdxx("encrypted-provider-id")).thenReturn(null);
        // 탈퇴 이력 비교용 식별값 해시를 생성하도록 결과를 구성함
        when(userIdEncryptionService.hashForAudit("12345")).thenReturn("hashed-provider-id");
        // 모든 과거 회원 번호의 유효 제재가 해제된 상태로 구성함
        when(userWithdrawalMapper.getActiveSuspCntByIdHash("hashed-provider-id")).thenReturn(0);
        // 탈퇴 전 닉네임과 관계없이 새 계정의 최초 닉네임을 발급하도록 구성함
        when(nicknameGenerationService.setGeneratedNickname()).thenReturn("새로운 독서가");
        // 신규 회원 저장 시 과거와 다른 새 회원 번호가 발급되도록 구성함
        doAnswer(this::setNewUserNumber).when(userMapper).setUser(any(UserDto.class));
        // 새 계정의 Kakao 프로필 파일 번호를 반환하도록 구성함
        when(fileService.setKakaoProfileImage("https://example.com/profile.png", "12345", 99L)).thenReturn(15L);
        // 새 회원에게 Access Token을 발급하도록 결과를 구성함
        when(jwtProvider.createAccessToken(eq(99L), eq(AuthConstant.ROLE_USER), anyString())).thenReturn("access-token");
        // 새 회원에게 Refresh Token을 발급하도록 결과를 구성함
        when(jwtProvider.createRefreshToken(eq(99L), anyString())).thenReturn("refresh-token");
        // Redis 로그인 세션의 유지 시간을 설정하도록 결과를 구성함
        when(jwtProvider.getRefreshTokenValidSec()).thenReturn(3600L);

        // 관리자 제재 해제 뒤 같은 Kakao 계정으로 로그인을 요청함
        ResultData result = authService.kakaoLogin("authorization-code", "127.0.0.1", "test-agent");

        // 제재 해제 뒤 신규 가입과 로그인이 정상 완료되는지 검증함
        assertEquals(200, result.getCode());
        // 과거 계정 복구가 아니라 새 회원 등록이 수행되는지 검증함
        verify(userMapper).setUser(any(UserDto.class));
    }

    /**
     * Kakao 기본 프로필 이미지는 서비스 프로필 파일로 저장하지 않는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws Exception Kakao 인증 응답 대역 구성 중 발생
     */
    @Test
    void skipsDefaultImage() throws Exception {
        // Kakao 토큰 교환 결과를 로그인 서비스에 제공할 객체를 생성함
        KakaoTokenDto kakaoToken = new KakaoTokenDto();
        // 기본 프로필 이미지 사용 상태가 포함된 Kakao 응답을 생성함
        KakaoAccountDto kakaoAccount = getKakaoAccount();
        // Kakao 기본 실루엣을 사용 중인 계정으로 표시함
        kakaoAccount.kakao_account.profile.is_default_image = true;

        // Kakao 인가 코드로 토큰을 교환하도록 결과를 구성함
        when(kakaoAuthProvider.getKakaoToken("default-profile-code")).thenReturn(kakaoToken);
        // 토큰으로 기본 프로필 이미지 상태를 조회하도록 결과를 구성함
        when(kakaoAuthProvider.getKakaoAccount(kakaoToken)).thenReturn(kakaoAccount);
        // 현재 회원 테이블 조회용 결정적 암호문을 생성하도록 결과를 구성함
        when(userIdEncryptionService.encryptForStorage("12345")).thenReturn("encrypted-provider-id");
        // 신규 가입 대상이라 현재 회원 행은 조회되지 않도록 구성함
        when(userMapper.getUserByIdxx("encrypted-provider-id")).thenReturn(null);
        // 탈퇴 이력 비교용 식별값 해시를 생성하도록 결과를 구성함
        when(userIdEncryptionService.hashForAudit("12345")).thenReturn("hashed-provider-id");
        // 같은 식별값의 유효 제재가 없도록 구성함
        when(userWithdrawalMapper.getActiveSuspCntByIdHash("hashed-provider-id")).thenReturn(0);
        // 신규 회원에게 최초 닉네임을 발급하도록 구성함
        when(nicknameGenerationService.setGeneratedNickname()).thenReturn("기본 사진 독서가");
        // 신규 회원 저장 시 회원 번호가 발급되도록 구성함
        doAnswer(this::setNewUserNumber).when(userMapper).setUser(any(UserDto.class));
        // 새 회원에게 Access Token을 발급하도록 결과를 구성함
        when(jwtProvider.createAccessToken(eq(99L), eq(AuthConstant.ROLE_USER), anyString())).thenReturn("access-token");
        // 새 회원에게 Refresh Token을 발급하도록 결과를 구성함
        when(jwtProvider.createRefreshToken(eq(99L), anyString())).thenReturn("refresh-token");
        // Redis 로그인 세션의 유지 시간을 설정하도록 결과를 구성함
        when(jwtProvider.getRefreshTokenValidSec()).thenReturn(3600L);

        // Kakao 기본 프로필 이미지를 사용하는 계정으로 로그인함
        ResultData result = authService.kakaoLogin("default-profile-code", "127.0.0.1", "test-agent");

        // 기본 프로필 이미지 상태에서도 회원 가입과 로그인이 정상 완료되는지 검증함
        assertEquals(200, result.getCode());
        // Kakao 기본 실루엣을 내부 프로필 파일로 저장하지 않는지 검증함
        verify(fileService, never()).setKakaoProfileImage(any(), anyString(), any());
        // 파일 번호가 없는 기본 프로필 상태를 회원 정보에 반영하는지 검증함
        verify(userMapper).uptUserProfile(any(UserDto.class));
    }

    /**
     * 비활성화 회원 복귀 테스트에 사용할 Kakao 계정 응답을 생성함
     *
     * @author SeungHyeon.Kang
     * @return 사용자 식별값과 프로필 이미지가 포함된 Kakao 응답
     */
    private KakaoAccountDto getKakaoAccount() {
        // Kakao 사용자 식별값을 포함할 계정 응답을 생성함
        KakaoAccountDto kakaoAccount = new KakaoAccountDto();
        // 로그인 사용자와 기존 회원을 연결할 Kakao 식별값을 설정함
        kakaoAccount.id = 12345L;

        // Kakao 계정의 프로필 응답 영역을 생성함
        KakaoAccountDto.KakaoAccount accountDetail = new KakaoAccountDto.KakaoAccount();
        // Kakao 프로필 이미지 응답을 생성함
        KakaoAccountDto.KakaoAccount.KakaoProfile profile = new KakaoAccountDto.KakaoAccount.KakaoProfile();
        // 기존 프로필이 없을 때 사용할 Kakao 이미지 주소를 설정함
        profile.profile_image_url = "https://example.com/profile.png";
        // 테스트 기본값은 사용자가 직접 설정한 Kakao 프로필 사진으로 지정함
        profile.is_default_image = false;
        // Kakao 계정 응답에 프로필을 설정함
        accountDetail.profile = profile;
        // 최상위 Kakao 응답에 계정 상세를 설정함
        kakaoAccount.kakao_account = accountDetail;

        // 비활성화 회원 로그인 테스트에 사용할 Kakao 계정 응답을 반환함
        return kakaoAccount;
    }

    /**
     * 신규 회원 저장 대역에 과거 계정과 다른 회원 번호를 설정함
     *
     * @author SeungHyeon.Kang
     * @param invocation 회원 등록 Mapper 호출 정보
     * @return 등록 처리 건수
     */
    private int setNewUserNumber(InvocationOnMock invocation) {
        // 저장 요청 회원 객체에 DB가 발급한 새 회원 번호를 반영함
        UserDto user = invocation.getArgument(0);
        // 물리 삭제된 과거 회원 번호 대신 새 번호를 설정함
        user.setUserNumb(99L);
        // MyBatis 등록 성공 건수를 반환함
        return 1;
    }
}
