package org.our.sadari.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * fileName       : JwtFilter
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더 꺼내기
        String header = request.getHeader("Authorization");

        // 2. Bearer 토큰인지 확인
        if (header != null && header.startsWith("Bearer ")) {

            // "Bearer " 이후 실제 토큰만 추출
            String token = header.substring(7);

            // 3. 토큰 검증
            if (jwtProvider.validateToken(token)) {

                // 4. Authentication 객체 생성
                Authentication authentication = jwtProvider.getAuthentication(token);

                // 5. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 6. 다음 필터로 넘김 (이거 없으면 요청 멈춤)
        filterChain.doFilter(request, response);
    }
}
