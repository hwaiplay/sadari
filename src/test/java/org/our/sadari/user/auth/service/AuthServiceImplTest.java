package org.our.sadari.user.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.service.UserIdEncryptionService;
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
import org.our.sadari.user.service.NicknameGenerationService;

/**
 * fileName       : AuthServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : Kakao 로그인 시 비활성화 계정 재활성화와 안내 상태 전달을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
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
    // Kakao 로그인과 계정 재활성화 검증 대상
    @InjectMocks
    private AuthServiceImpl authService;

    /**
     * 비활성화 회원이 같은 Kakao 계정으로 로그인하면 정상 상태로 복구하고 복귀 안내 여부를 반환하는지 검증한다
     *
     * @author SeungHyeon.Kang
     * @throws Exception Kakao 인증 응답 대역 구성 중 발생
     */
    @Test
    void kakaoLoginReactivatesWithdrawnAccount() throws Exception {
        // Kakao 토큰 교환 결과를 로그인 서비스에 제공할 객체를 생성한다
        KakaoTokenDto kakaoToken = new KakaoTokenDto();
        // Kakao 프로필 이미지가 포함된 계정 응답을 생성한다
        KakaoAccountDto kakaoAccount = getKakaoAccount();

        UserDto savedUser = new UserDto();
        // 비활성화된 기존 회원 번호를 설정한다
        savedUser.setUserNumb(31L);
        // 기존 회원 권한을 설정한다
        savedUser.setUserRole(AuthConstant.ROLE_USER);
        // 기존 회원 닉네임을 설정한다
        savedUser.setUserNick("돌아온 독서가");
        // 재로그인 전 비활성화 상태를 설정한다
        savedUser.setUserStat(Constant.USER_STAT_WITHDRAWN);
        // 기존 프로필 파일이 유지되도록 파일 번호를 설정한다
        savedUser.setProfNumb(7L);
        // 비활성화 요청일을 설정한다
        savedUser.setWthdDate(LocalDateTime.now());

        // Kakao 인가 코드로 토큰을 교환하도록 결과를 구성한다
        when(kakaoAuthProvider.getKakaoToken("authorization-code")).thenReturn(kakaoToken);
        // Kakao 토큰으로 동일 계정의 프로필을 조회하도록 결과를 구성한다
        when(kakaoAuthProvider.getKakaoAccount(kakaoToken)).thenReturn(kakaoAccount);
        // Kakao 식별값을 DB 조회용 결정적 암호문으로 변환하도록 결과를 구성한다
        when(userIdEncryptionService.encryptForStorage("12345")).thenReturn("encrypted-provider-id");
        // 암호화한 Kakao 식별값으로 비활성화 회원이 조회되도록 결과를 구성한다
        when(userMapper.getUserByIdxx("encrypted-provider-id")).thenReturn(savedUser);
        // 재활성화된 회원에게 Access Token을 발급하도록 결과를 구성한다
        when(jwtProvider.createAccessToken(31L, AuthConstant.ROLE_USER)).thenReturn("access-token");
        // 재활성화된 회원에게 Refresh Token을 발급하도록 결과를 구성한다
        when(jwtProvider.createRefreshToken(31L)).thenReturn("refresh-token");
        // Redis 로그인 세션의 유지 시간을 설정하도록 결과를 구성한다
        when(jwtProvider.getRefreshTokenValiditySeconds()).thenReturn(3600L);

        // 같은 Kakao 계정으로 비활성화 회원의 재로그인을 요청한다
        ResultData result = authService.kakaoLogin("authorization-code", "127.0.0.1", "test-agent");
        // 로그인 성공 응답에 포함된 토큰과 복귀 상태를 조회한다
        TokenDto token = (TokenDto) result.getData();

        // 비활성화 계정 재로그인이 정상 성공 코드로 처리되는지 검증한다
        assertEquals(200, result.getCode());
        // OAuth 완료 화면이 복귀 정책을 안내할 수 있도록 재활성화 여부가 전달되는지 검증한다
        assertTrue(token.isAccountReactivated());
        // 기존 회원 상태가 정상 이용 상태로 변경되는지 검증한다
        assertEquals(Constant.USER_STAT_ACTIVE, savedUser.getUserStat());
        // 비활성화 요청일이 재활성화 시 제거되는지 검증한다
        assertNull(savedUser.getWthdDate());
        // 변경된 사용자 상태가 DB에 저장되는지 검증한다
        verify(userMapper).uptUserStatus(savedUser);
        // Redis 로그인 상태에도 재활성화된 회원 상태가 저장되는지 검증한다
        verify(tokenRedisService).setLoginUserInfo(
                31L
              , "refresh-token"
              , "돌아온 독서가"
              , Constant.USER_STAT_ACTIVE
              , 3600L
        );
    }

    /**
     * 비활성화 회원 복귀 테스트에 사용할 Kakao 계정 응답을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 사용자 식별값과 프로필 이미지가 포함된 Kakao 응답
     */
    private KakaoAccountDto getKakaoAccount() {
        // Kakao 사용자 식별값을 포함할 계정 응답을 생성한다
        KakaoAccountDto kakaoAccount = new KakaoAccountDto();
        // 로그인 사용자와 기존 회원을 연결할 Kakao 식별값을 설정한다
        kakaoAccount.id = 12345L;

        // Kakao 계정의 프로필 응답 영역을 생성한다
        KakaoAccountDto.KakaoAccount accountDetail = new KakaoAccountDto.KakaoAccount();
        // Kakao 프로필 이미지 응답을 생성한다
        KakaoAccountDto.KakaoAccount.KakaoProfile profile = new KakaoAccountDto.KakaoAccount.KakaoProfile();
        // 기존 프로필이 없을 때 사용할 Kakao 이미지 주소를 설정한다
        profile.profile_image_url = "https://example.com/profile.png";
        // Kakao 계정 응답에 프로필을 설정한다
        accountDetail.profile = profile;
        // 최상위 Kakao 응답에 계정 상세를 설정한다
        kakaoAccount.kakao_account = accountDetail;

        // 비활성화 회원 로그인 테스트에 사용할 Kakao 계정 응답을 반환한다
        return kakaoAccount;
    }
}
