package org.our.sadari.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.service.UserIdEncryptionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.auth.provider.KakaoAuthProvider;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.dto.UserWithdrawalDto;
import org.our.sadari.user.mapper.UserMapper;
import org.our.sadari.user.mapper.UserWithdrawalMapper;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * fileName       : UserWithdrawalServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 정지 회원의 영구 탈퇴 허용과 비활성화 제한 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class UserWithdrawalServiceImplTest {

    // 회원 탈퇴 이력 데이터 접근 객체 대역
    @Mock
    private UserWithdrawalMapper userWithdrawalMapper;
    // 회원 데이터 접근 객체 대역
    @Mock
    private UserMapper userMapper;
    // Kakao 재인증 제공 객체 대역
    @Mock
    private KakaoAuthProvider kakaoAuthProvider;
    // OAuth 식별값 변환 서비스 대역
    @Mock
    private UserIdEncryptionService userIdEncryptionService;
    // 탈퇴 공통코드 검증 대역
    @Mock
    private CodeUtil codeUtil;
    // 로그인 세션 서비스 대역
    @Mock
    private TokenRedisService tokenRedisService;
    // 일회성 재인증 상태 Redis 대역
    @Mock
    private StringRedisTemplate redisTemplate;
    // Redis 문자열 값 연산 대역
    @Mock
    private ValueOperations<String, String> valueOperations;
    // 재인증 요청 직렬화 대역
    @Mock
    private ObjectMapper objectMapper;
    // 정지 상태 복구 판정 서비스 대역
    @Mock
    private UserSuspensionService userSuspensionService;
    // 탈퇴 시 프로필 파일 처리 서비스 대역
    @Mock
    private FileService fileService;
    // 정지 회원의 탈퇴 유형 검증 대상
    @InjectMocks
    private UserWithdrawalServiceImpl userWithdrawalService;

    /**
     * 정지 회원과 재인증 요청에 공통으로 사용할 테스트 대역을 초기화한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통 실패 응답에서 사용할 테스트 메시지를 등록한다
        StaticMessageSource messageSource = new StaticMessageSource();
        // 잘못된 탈퇴 유형의 사용자 안내 메시지 키를 등록한다
        messageSource.addMessage("common.alert.0009", Locale.KOREAN, "요청값이 올바르지 않아요.");
        // 테스트 실행 JVM 기본 언어에서도 같은 검증 메시지를 조회하도록 등록한다
        messageSource.addMessage("common.alert.0009", Locale.getDefault(), "요청값이 올바르지 않아요.");
        // 공통 결과 객체가 테스트 메시지를 조회할 수 있도록 초기화한다
        new MessageUtils().setMessageSource(messageSource);

        // 탈퇴 유형과 사유가 모두 활성 공통코드로 조회되도록 구성한다
        when(codeUtil.existsCode(anyString(), anyString())).thenReturn(true);
        // 로그인 회원이 현재 정지 상태로 조회되도록 구성한다
        UserDto suspendedUser = new UserDto();
        // 영구 탈퇴 뒤에도 제재 이력과 연결할 회원 번호를 설정한다
        suspendedUser.setUserNumb(31L);
        // 정지 중 허용 유형을 구분할 현재 회원 상태를 설정한다
        suspendedUser.setUserStat(Constant.USER_STAT_SUSPENDED);
        // 탈퇴 요청 회원 번호로 정지 회원을 반환하도록 구성한다
        when(userMapper.getUserByNumb(31L)).thenReturn(suspendedUser);
    }

    /**
     * 정지 회원의 영구 탈퇴는 Kakao 재인증 단계까지 진행되는지 검증한다
     *
     * @author SeungHyeon.Kang
     * @throws Exception 재인증 요청 직렬화 대역 구성 중 발생
     */
    @Test
    void setWithdrawalRequestAllowsHardType() throws Exception {
        // 정지 회원에게 허용할 영구 탈퇴 요청을 생성한다
        UserWithdrawalDto request = createRequest(Constant.WITHDRAWAL_TYPE_HARD);
        // 일회성 OAuth 상태를 Redis에 저장할 연산 객체를 반환하도록 구성한다
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 검증된 탈퇴 요청을 Redis 문자열로 직렬화하도록 구성한다
        when(objectMapper.writeValueAsString(request)).thenReturn("{}");
        // 일회성 상태값을 포함한 Kakao 재인증 URL을 반환하도록 구성한다
        when(kakaoAuthProvider.getKakaoAuthorizationUrl(anyString())).thenReturn("https://kakao.example/authorize");

        // 정지 회원의 영구 탈퇴 재인증 요청을 수행한다
        ResultData result = userWithdrawalService.setWithdrawalRequest(31L, request);

        // 정지 중 영구 탈퇴 요청이 정상 처리되는지 검증한다
        assertEquals(200, result.getCode());
        // 일회성 재인증 요청이 제한 시간과 함께 Redis에 저장되는지 검증한다
        verify(valueOperations).set(anyString(), eq("{}"), eq(Duration.ofMinutes(10)));
    }

    /**
     * 정지 회원의 복구 가능한 계정 비활성화는 계속 제한되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setWithdrawalRequestBlocksSoftType() {
        // 정지 회원에게 허용하지 않을 계정 비활성화 요청을 생성한다
        UserWithdrawalDto request = createRequest(Constant.WITHDRAWAL_TYPE_SOFT);

        // 정지 회원의 계정 비활성화 재인증 요청을 수행한다
        ResultData result = userWithdrawalService.setWithdrawalRequest(31L, request);

        // 복구 가능한 비활성화 요청은 잘못된 요청으로 거절되는지 검증한다
        assertEquals(2009, result.getCode());
        // 거절된 요청은 Redis 재인증 상태를 생성하지 않는지 검증한다
        verify(redisTemplate, never()).opsForValue();
    }

    /**
     * 정지 회원의 탈퇴 유형별 요청 객체를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param withdrawalType 검증할 계정 처리 유형
     * @return 필수 사유가 포함된 탈퇴 요청
     */
    private UserWithdrawalDto createRequest(String withdrawalType) {
        // 유형별 검증에 사용할 최소 탈퇴 요청 객체를 생성한다
        UserWithdrawalDto request = new UserWithdrawalDto();
        // 정지 회원에게 허용하거나 제한할 계정 처리 유형을 설정한다
        request.setWthdType(withdrawalType);
        // 공통코드 검증을 통과할 기본 탈퇴 사유를 설정한다
        request.setWthdRson("LOW_USAGE");
        // 정지 회원 정책 검증에 사용할 탈퇴 요청을 반환한다
        return request;
    }
}
