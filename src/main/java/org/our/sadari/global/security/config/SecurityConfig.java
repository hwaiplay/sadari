package org.our.sadari.global.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.our.sadari.global.security.jwt.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
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
 * Spring Security 및 JWT 기반 보안 설정을 담당하는 클래스
 *
 * @author Seunghyeon.Kang
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${domain.front}")
    private String FRONT_DOMAIN; // CORS 허용을 위한 프론트엔드 도메인 주소

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // REST API 환경이므로 불필요한 CSRF Protection 비활성화
                .csrf(csrf -> csrf.disable())

                // JWT 기반 인증을 사용하므로 세션을 생성하지 않고 Stateless 상태로 관리
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 커스텀 CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 기본 제공 폼 로그인 및 HTTP Basic 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 요청 URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 인증 없이 접근을 허용할 공개 API Endpoint 목록
                        .requestMatchers(
                                "/api/oauth/callback/kakao",
                                "/api/oauth/refresh",
                                "/api/oauth/logout",
                                "/api/oauth/tokenCheck",
                                "/uploads/**",
                                // 도서 검색 API
                                "/api/book/search"
                        ).permitAll()

                        // 관리자 권한(ADMIN)을 가진 사용자만 접근 가능
                        .requestMatchers(
                                "/api/admin/**",
                                // Swagger UI 및 OpenAPI 문서는 내부 API 정의서이므로 관리자만 접근을 허용한다.
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).hasRole("ADMIN")

                        // 그 외 모든 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )

                // 인증/인가 예외 처리 (Custom Exception Handling)
                .exceptionHandling(ex -> ex
                        // 미인증 사용자 접근 시 401 Unauthorized 반환
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        // 권한 부족 시 403 Forbidden 반환
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        })
                )

                // UsernamePasswordAuthenticationFilter 이전에 커스텀 JwtFilter 실행
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 세부 정책 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(FRONT_DOMAIN)); // 지정된 프론트엔드 도메인만 접근 허용
        config.setAllowedMethods(List.of("*"));           // 모든 HTTP Method 허용
        config.setAllowedHeaders(List.of("*"));           // 모든 헤더 허용
        config.setAllowCredentials(true);                 // 자격 증명(쿠키, Authorization 헤더 등) 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);   // 전체 경로에 CORS 정책 적용

        return source;
    }
}
