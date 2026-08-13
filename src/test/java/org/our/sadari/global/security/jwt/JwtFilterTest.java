package org.our.sadari.global.security.jwt;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : JwtFilterTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 제한 계정 상태의 CSRF Token 조회 경로를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    // JWT 제공 객체 Mock
    @Mock
    private JwtProvider jwtProvider;
    // Redis Token 서비스 Mock
    @Mock
    private TokenRedisService tokenRedisService;
    // 사용자 데이터 접근 Mock
    @Mock
    private UserMapper userMapper;
    // JWT 필터 단위 테스트 대상
    @InjectMocks
    private JwtFilter jwtFilter;

    /** 정지 회원이 허용된 상태 변경 요청을 위해 CSRF Token을 조회할 수 있다. */
    @Test
    void suspendedUserCanGetCsrfToken() {
        Boolean allowed = ReflectionTestUtils.invokeMethod(
                jwtFilter, "isSuspendedAllowedPath", "/api/oauth/csrf"
        );

        assertTrue(Boolean.TRUE.equals(allowed));
    }
}
