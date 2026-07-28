package org.our.sadari.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * fileName       : JwtFilter
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : 인증과 보안 요청의 인증 상태를 검사한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang    최초 생성
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // 접근 TOKEN COOKIE 명칭 설정값
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    // REFRESH TOKEN API URI 설정값
    private static final String REFRESH_TOKEN_API_URI = "/api/oauth/refresh";

    // Jwt 외부 연동 제공 객체
    private final JwtProvider jwtProvider;
    // TokenRedis 업무 처리 서비스
    private final TokenRedisService tokenRedisService;

    /**
     * HTTP 요청 헤더/쿠키에서 Access Token을 추출하여 유효성 및 블랙리스트 등록 여부를 검증한 후 SecurityContext에 인증 객체를 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param request 서블릿 요청 객체
     * @param response 서블릿 응답 객체
     * @param filterChain 서블릿 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // extractAccessToken 호출로 요청에서 인증 토큰을 추출한다
        String token = extractAccessToken(request);

        // Access Token이 존재하고, 서명/만료시간이 유효하며, Redis 블랙리스트(로그아웃된 토큰)에 등록되지 않은 경우 인증 객체를 생성한다.
        if (!StringUtil.isEmpty(token) && jwtProvider.validateToken(token) && !tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(token))) {
            // getAuthentication 조회로 후속 처리에 필요한 데이터를 가져온다
            Authentication authentication = jwtProvider.getAuthentication(token);
            // SecurityContext에 Authentication 객체를 세팅하여 이 후 컨트롤러에서 @AuthenticationPrincipal 등으로 유저 정보를 참조할 수 있게 한다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // JWT 검증 뒤 남은 보안 필터 체인을 계속 실행한다
        filterChain.doFilter(request, response);
    }

    /**
     * 특정 요청 URI에 대해 해당 JWT 필터 수행을 건너뛸지 여부를 결정한다.
     * Refresh Token 재발급 API(/api/oauth/refresh)는 만료된 Access Token 상태로 들어오므로 검증 대상에서 제외한다.
     *
     * @author SeungHyeon.Kang
     * @param request 서블릿 요청 객체
     * @return 필터 제외 여부 (true: 필터 미실행, false: 필터 실행)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 특정 요청 URI에 대해 해당 JWT 필터 수행을 건너뛸지 여부를 결정 결과를 반환한다
        return REFRESH_TOKEN_API_URI.equals(request.getRequestURI());
    }

    /**
     * HTTP 요청의 쿠키 목록에서 Access Token 쿠키 값을 추출한다.
     *
     * @author SeungHyeon.Kang
     * @param request 서블릿 요청 객체
     * @return 추출된 Access Token 문자열 (존재하지 않을 경우 null)
     */
    private String extractAccessToken(HttpServletRequest request) {
        // 요청 헤더에 쿠키가 존재하지 않는 경우 null을 반환한다.
        if (StringUtil.isEmpty(request.getCookies())) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (Cookie cookie : request.getCookies()) {
            // Access Token 쿠키명과 일치하는 쿠키가 존재하면 해당 토큰 값을 반환한다.
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                // HTTP 요청의 쿠키 목록에서 Access Token 쿠키 값을 추출 결과를 반환한다
                return cookie.getValue();
            }
        }

        // 조회하거나 생성할 값이 없음을 반환한다
        return null;
    }
}
