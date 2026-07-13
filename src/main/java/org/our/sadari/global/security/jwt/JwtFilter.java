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
 * fileName       : JwtFilter
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : 쿠키 기반 JWT 인증 필터
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;

    /**
     * 요청 쿠키의 accessToken을 검증하고 Spring Security 인증 컨텍스트를 구성한다.
     * JWT 서명과 만료 시간이 정상이어도 Redis blacklist에 등록된 토큰이면 로그아웃된 토큰으로 보고 인증하지 않는다.
     * 토큰이 없거나 검증에 실패한 경우 여기서 직접 응답을 종료하지 않고 SecurityConfig의 인증 실패 처리로 넘긴다.
     * @Author Seunghyeon.Kang
     * @param request accessToken 쿠키가 포함될 수 있는 HTTP 요청 객체
     * @param response 인증 실패 또는 성공 이후 이어질 HTTP 응답 객체
     * @param filterChain 다음 필터로 요청 처리를 넘기기 위한 필터 체인 객체
     * @return
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        if (!StringUtil.isEmpty(request.getCookies())) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    // accessToken 쿠키를 찾으면 더 이상 다른 쿠키를 탐색하지 않는다.
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (!StringUtil.isEmpty(token) && jwtProvider.validateToken(token) && !tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(token))) {
            // 쿠키 존재, JWT 검증, Redis blacklist 검증을 모두 통과한 경우에만 인증 객체를 만든다.
            Authentication authentication = jwtProvider.getAuthentication(token);

            // 이후 컨트롤러에서 @AuthenticationPrincipal로 사용자 번호를 받을 수 있도록 인증 정보를 저장한다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 토큰이 없거나 검증에 실패해도 여기서 응답을 끊지 않고 다음 필터와 SecurityConfig 판단에 맡긴다.
        filterChain.doFilter(request, response);
    }

    /**
     * accessToken 없이 refreshToken만으로 처리해야 하는 재발급 API는 JWT 필터를 적용하지 않는다.
     * 재발급 API 내부에서 refreshToken 쿠키와 Redis 저장값을 직접 검증하므로 이 필터의 accessToken 검증 대상에서 제외한다.
     * 다른 API는 필터를 통과시켜 accessToken 기반 인증 여부를 판단한다.
     * @Author Seunghyeon.Kang
     * @param request 필터 적용 제외 여부를 확인할 HTTP 요청 객체
     * @return JWT 필터를 건너뛰어야 하면 true, 필터를 적용해야 하면 false
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/api/oauth/refresh");
    }
}
