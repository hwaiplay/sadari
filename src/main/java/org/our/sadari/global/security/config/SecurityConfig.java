package org.our.sadari.global.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.our.sadari.global.security.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * fileName       : SecurityConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 * 2026-03-23        hanwon.Jang           CORS 설정 추가
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //  CSRF 비활성화
                // JWT는 세션을 사용하지 않기 때문에 필요 없음
                .csrf(csrf -> csrf.disable())

                //  세션 사용 안 함 (stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CORS 설정 (WebConfig에서 설정한 CORS 정책 적용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //  기본 로그인 방식 제거
                // (Spring Security 기본 로그인 페이지 방지)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 인증 없이 허용할 API
                        .requestMatchers(
                                "/api/oauth/callback/kakao",  // 🔥 이거 추가
                                "/api/oauth/refresh",
                                "/api/oauth/tokenCheck",
                                "/api/book/search" // 책 검색 API는 인증 없이 접근 허용
                        ).permitAll()

                        
                        // 관리자 권한
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 나머지는 전부 인증 필요
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        //  인증 실패 → 401
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        //  권한 부족 → 403
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        })
                )

                // JWT 필터 등록 (핵심)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정을 위한 Bean 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}