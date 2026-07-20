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
 * 매 HTTP 요청마다 실행되어 쿠키의 Access Token 검증 및 Spring Security 인증 객체(Authentication) 등록을 수행하는 서블릿 필터.
 *
 * @author Seunghyeon.Kang
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_API_URI = "/api/oauth/refresh";

    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;

    /**
     * HTTP 요청 헤더/쿠키에서 Access Token을 추출하여 유효성 및 블랙리스트 등록 여부를 검증한 후 SecurityContext에 인증 객체를 등록한다.
     *
     * @author Seunghyeon.Kang
     * @param request 서블릿 요청 객체
     * @param response 서블릿 응답 객체
     * @param filterChain 서블릿 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractAccessToken(request);

        // Access Token이 존재하고, 서명/만료시간이 유효하며, Redis 블랙리스트(로그아웃된 토큰)에 등록되지 않은 경우 인증 객체를 생성한다.
        if (!StringUtil.isEmpty(token) && jwtProvider.validateToken(token) && !tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(token))) {
            Authentication authentication = jwtProvider.getAuthentication(token);
            // SecurityContext에 Authentication 객체를 세팅하여 이 후 컨트롤러에서 @AuthenticationPrincipal 등으로 유저 정보를 참조할 수 있게 한다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 특정 요청 URI에 대해 해당 JWT 필터 수행을 건너뛸지 여부를 결정한다.
     * Refresh Token 재발급 API(/api/oauth/refresh)는 만료된 Access Token 상태로 들어오므로 검증 대상에서 제외한다.
     *
     * @author Seunghyeon.Kang
     * @param request 서블릿 요청 객체
     * @return 필터 제외 여부 (true: 필터 미실행, false: 필터 실행)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return REFRESH_TOKEN_API_URI.equals(request.getRequestURI());
    }

    /**
     * HTTP 요청의 쿠키 목록에서 Access Token 쿠키 값을 추출한다.
     *
     * @author Seunghyeon.Kang
     * @param request 서블릿 요청 객체
     * @return 추출된 Access Token 문자열 (존재하지 않을 경우 null)
     */
    private String extractAccessToken(HttpServletRequest request) {
        // 요청 헤더에 쿠키가 존재하지 않는 경우 null을 반환한다.
        if (StringUtil.isEmpty(request.getCookies())) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            // Access Token 쿠키명과 일치하는 쿠키가 존재하면 해당 토큰 값을 반환한다.
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}