package org.our.sadari.security.config;

import lombok.RequiredArgsConstructor;
import org.our.sadari.security.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * fileName       : SecurityConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. CSRF 비활성화
                // JWT는 세션을 사용하지 않기 때문에 필요 없음
                .csrf(csrf -> csrf.disable())

                // 2. 세션 사용 안 함 (stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 기본 로그인 방식 제거
                // (Spring Security 기본 로그인 페이지 방지)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 4. URL별 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 인증 없이 허용할 API
                        .requestMatchers(
                                "/api/oauth/callback/kakao",  // 🔥 이거 추가
                                "/api/oauth/refresh"
                        ).permitAll()

                        // 나머지는 전부 인증 필요
                        .anyRequest().authenticated()
                )

                // 5. JWT 필터 등록 (핵심)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}