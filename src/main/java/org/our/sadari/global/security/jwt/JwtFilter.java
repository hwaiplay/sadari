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
 * JwtFilter 클래스의 역할과 책임을 정의한다.
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
     * doFilterInternal 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @param response 처리에 필요한 입력값
     * @param filterChain 처리에 필요한 입력값
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractAccessToken(request);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (!StringUtil.isEmpty(token)
                && jwtProvider.validateToken(token)
                && !tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(token))) {
            Authentication authentication = jwtProvider.getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * shouldNotFilter 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @return 처리 결과
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return REFRESH_TOKEN_API_URI.equals(request.getRequestURI());
    }

    /**
     * extractAccessToken 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param request 처리에 필요한 입력값
     * @return 처리 결과
     */
    private String extractAccessToken(HttpServletRequest request) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(request.getCookies())) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
