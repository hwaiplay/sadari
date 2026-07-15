package org.our.sadari.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
 * accessToken 쿠키를 읽어 Spring Security 인증 객체를 구성하는 JWT 필터입니다.
 *
 * @author Seunghyeon.Kang
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;

    /**
     * 요청 쿠키에서 accessToken을 추출하고 JWT 검증과 Redis blacklist 검증을 수행합니다.
     *
     * @author Seunghyeon.Kang
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 다음 필터로 요청을 넘기기 위한 필터 체인
     * @throws ServletException 필터 처리 중 Servlet 오류가 발생한 경우
     * @throws IOException 필터 처리 중 IO 오류가 발생한 경우
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // accessToken은 HttpOnly 쿠키에 저장되므로 요청 쿠키 배열에서 직접 찾아야 합니다.
        if (!StringUtil.isEmpty(request.getCookies())) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // JWT 자체가 유효하고 logout blacklist에 없는 경우에만 SecurityContext에 인증 객체를 저장합니다.
        if (!StringUtil.isEmpty(token) && jwtProvider.validateToken(token) && !tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(token))) {
            Authentication authentication = jwtProvider.getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * refreshToken으로 accessToken을 재발급받는 API는 accessToken 필터 검증에서 제외합니다.
     *
     * @author Seunghyeon.Kang
     * @param request HTTP 요청 객체
     * @return JWT 필터를 건너뛸 요청이면 true
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/api/oauth/refresh");
    }
}
