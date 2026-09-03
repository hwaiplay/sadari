package org.our.sadari.user.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.context.support.StaticMessageSource;

/**
 * fileName       : LocalAuthServiceTest
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 로컬 개발용 로그인의 계정 상태 검증과 세션 발급을 확인한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 */
@ExtendWith(MockitoExtension.class)
class LocalAuthServiceTest {

    // JWT 발급 제공 객체 대역
    @Mock
    private JwtProvider jwtProvider;
    // 로그인 세션 Redis 서비스 대역
    @Mock
    private TokenRedisService tokenRedisService;
    // 사용자 데이터 접근 객체 대역
    @Mock
    private UserMapper userMapper;
    // 로컬 개발용 로그인 검증 대상
    @InjectMocks
    private LocalAuthService localAuthService;

    /**
     * 공통 인증 실패 응답에 사용할 테스트 메시지 소스를 초기화한다
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUpMessageSource() {
        // 공통 결과 객체가 인증 실패 문구를 조회할 메시지 소스를 생성한다
        StaticMessageSource messageSource = new StaticMessageSource();
        // 한국어 인증 실패 메시지를 테스트 메시지 소스에 등록한다
        messageSource.addMessage("auth.common.fail", Locale.KOREAN, "인증에 실패했습니다.");
        // 테스트 JVM 기본 언어에서도 인증 실패 메시지를 조회하도록 등록한다
        messageSource.addMessage("auth.common.fail", Locale.getDefault(), "인증에 실패했습니다.");
        // 공통 응답이 테스트 메시지 소스를 사용하도록 연결한다
        new MessageUtils().setMessageSource(messageSource);
    }

    /**
     * 활성 회원은 DB 권한으로 JWT와 Redis 로그인 세션을 발급받는다
     *
     * @author HanWon.Jang
     */
    @Test
    void activeUserGetsSession() {
        // 활성 회원의 DB 원본 정보를 구성한다
        UserDto savedUser = getUser(Constant.USER_STAT_ACTIVE);
        // 요청 회원 번호로 활성 회원이 조회되도록 구성한다
        when(userMapper.getUserByNumb(101L)).thenReturn(savedUser);
        // 활성 회원의 Access Token 발급 결과를 구성한다
        when(jwtProvider.createAccessToken(eq(101L), eq(AuthConstant.ROLE_USER), anyString()))
                .thenReturn("access-token");
        // 활성 회원의 Refresh Token 발급 결과를 구성한다
        when(jwtProvider.createRefreshToken(eq(101L), anyString())).thenReturn("refresh-token");
        // Redis 로그인 세션 유지 시간을 구성한다
        when(jwtProvider.getRefreshTokenValidSec()).thenReturn(3600L);

        // 활성 회원 번호로 로컬 개발용 로그인을 요청한다
        ResultData result = localAuthService.setLocalLogin(101L);
        // 로그인 결과에 발급된 토큰 데이터를 조회한다
        TokenDto token = (TokenDto) result.getData();

        // 활성 회원 로그인이 공통 성공 코드로 처리되는지 확인한다
        assertEquals(200, result.getCode());
        // 발급한 Access Token이 브라우저 쿠키 전달 데이터에 포함되는지 확인한다
        assertEquals("access-token", token.getAccessToken());
        // 발급한 Refresh Token이 브라우저 쿠키 전달 데이터에 포함되는지 확인한다
        assertEquals("refresh-token", token.getRefreshToken());
        // 로컬 간편 로그인이 계정 재활성화로 표시되지 않는지 확인한다
        assertFalse(token.isAccountReactivated());
        // DB 권한과 현재 상태를 사용한 Redis 로그인 세션이 생성되는지 확인한다
        verify(tokenRedisService).setLoginUserInfo(
                eq(101L)
              , anyString()
              , eq("refresh-token")
              , eq("테스트 사용자")
              , eq(Constant.USER_STAT_ACTIVE)
              , eq(3600L)
        );
    }

    /**
     * 활성 상태가 아닌 회원은 로컬 개발용 로그인으로 계정 제한을 우회하지 못한다
     *
     * @author HanWon.Jang
     * @param userStat 로그인 차단을 확인할 회원 상태
     */
    @ParameterizedTest
    @ValueSource(strings = {"WITHDRAWN", "DELETE_PENDING", "SUSPENDED"})
    void restrictedUserGetsFailure(String userStat) {
        // 제한 상태 회원의 DB 원본 정보를 구성한다
        UserDto savedUser = getUser(userStat);
        // 요청 회원 번호로 제한 상태 회원이 조회되도록 구성한다
        when(userMapper.getUserByNumb(101L)).thenReturn(savedUser);

        // 제한 상태 회원 번호로 로컬 개발용 로그인을 요청한다
        ResultData result = localAuthService.setLocalLogin(101L);

        // 제한 상태를 공통 인증 실패 코드로 처리해 계정 존재 여부를 노출하지 않는지 확인한다
        assertEquals(1001, result.getCode());
        // 차단된 로그인에서 JWT나 Redis 세션을 만들지 않는지 확인한다
        verifyNoInteractions(jwtProvider, tokenRedisService);
    }

    /**
     * 계정 상태별 로컬 로그인 검증에 사용할 회원 정보를 생성한다
     *
     * @author HanWon.Jang
     * @param userStat 구성할 회원 상태
     * @return 지정한 상태와 일반 사용자 권한을 가진 회원 정보
     */
    private UserDto getUser(String userStat) {
        // 로그인 검증에 사용할 회원 DTO를 생성한다
        UserDto savedUser = new UserDto();
        // 테스트 회원 번호를 설정한다
        savedUser.setUserNumb(101L);
        // DB에서 조회한 일반 사용자 권한을 설정한다
        savedUser.setUserRole(AuthConstant.ROLE_USER);
        // Redis 닉네임 캐시에 사용할 테스트 닉네임을 설정한다
        savedUser.setUserNick("테스트 사용자");
        // 활성 또는 제한 상태 검증에 사용할 회원 상태를 설정한다
        savedUser.setUserStat(userStat);
        // 상태 검증에 사용할 회원 정보를 반환한다
        return savedUser;
    }
}
