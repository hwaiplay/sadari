package org.our.sadari.global.security.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.our.sadari.global.security.jwt.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * fileName       : SecurityConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : 인증과 보안 실행 설정을 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang    최초 생성
 * 2026-08-04        SeungHyeon.Kang    Cookie 인증 API CSRF 보호 적용
 * 2026-08-27        SeungHyeon.Kang    서버 오류 화면 접근 허용
 * 2026-09-03        HanWon.Jang        로컬 프로필 간편 로그인 허용
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // FRONT DOMAIN 설정값
    @Value("${domain.front}")
    private String FRONT_DOMAIN; // CORS 허용을 위한 프론트엔드 도메인 주소

    // HTTPS에서만 CSRF Cookie를 전송할지 여부
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    // CSRF Cookie의 SameSite 정책
    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    // jwt filter
    private final JwtFilter jwtFilter;

    /**
     * JWT 인증과 API 접근 권한을 적용한 SecurityFilterChain을 구성한다
     *
     * @author SeungHyeon.Kang
     * @param http API 접근 규칙을 설정할 HttpSecurity
     * @param csrfTokenRepository CSRF Token을 Cookie에 저장할 Repository
     * @return 구성하거나 조회한 결과 객체
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CookieCsrfTokenRepository csrfTokenRepository) throws Exception {

        http
                // 브라우저가 자동 전송하는 인증 Cookie와 별도로 요청 Header의 CSRF Token을 검증한다
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))

                // JWT 기반 인증을 사용하므로 세션을 생성하지 않고 Stateless 상태로 관리
                .sessionManagement(session ->
                        // JWT 인증에 맞게 서버 세션을 생성하지 않도록 설정한다
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 커스텀 CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 기본 제공 폼 로그인 및 HTTP Basic 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 요청 URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 서버 예외를 전용 오류 문서로 표시할 때 인증 필터가 오류 디스패치를 차단하지 않도록 허용한다
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // 인증 없이 접근을 허용할 공개 API Endpoint 목록
                        .requestMatchers(
                                "/api/oauth/kakao",
                                "/api/oauth/callback/**",
                                "/api/oauth/local-login",
                                "/api/oauth/csrf",
                                "/api/oauth/refresh",
                                "/api/oauth/logout",
                                "/api/oauth/tokenCheck",
                                "/error/500.html"
                        // 인증 없이 접근 가능한 공개 API 경로를 설정한다
                        ).permitAll()

                        // 관리자 권한(ADMIN)을 가진 사용자만 접근 가능
                        .requestMatchers(
                                "/api/admin/**",
                                // Swagger UI 및 OpenAPI 문서는 내부 API 정의서이므로 관리자만 접근을 허용한다.
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        // Swagger 문서 접근을 관리자 권한으로 제한한다
                        ).hasRole("ADMIN")

                        // 그 외 모든 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )

                // 인증/인가 예외 처리 (Custom Exception Handling)
                .exceptionHandling(ex -> ex
                        // 미인증 사용자 접근 시 401 Unauthorized 반환
                        .authenticationEntryPoint((req, res, e) -> {
                            // Status 업무 값을 res DTO에 설정한다
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        // 권한 부족 시 403 Forbidden 반환
                        .accessDeniedHandler((req, res, e) -> {
                            // Status 업무 값을 res DTO에 설정한다
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        })
                )

                // UsernamePasswordAuthenticationFilter 이전에 커스텀 JwtFilter 실행
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // JWT 인증과 API 접근 권한을 적용한 SecurityFilterChain을 구성 결과를 반환한다
        return http.build();
    }

    /**
     * 인증 Cookie와 같은 환경 속성을 사용하는 CSRF Cookie Repository를 구성한다.
     *
     * @author SeungHyeon.Kang
     * @return CSRF Token을 HttpOnly Cookie로 저장하는 Repository
     */
    @Bean
    public CookieCsrfTokenRepository getCsrfTokenRepository() {
        // CSRF Token을 브라우저 Cookie에 저장할 Repository를 생성한다
        CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        // 인증 Cookie와 같은 전송 범위를 적용해 환경별 교차 출처 구성을 유지한다
        repository.setCookieCustomizer(this::uptCsrfCookie);
        // CSRF Token을 HttpOnly Cookie에 저장하는 Repository를 반환한다
        return repository;
    }

    /**
     * 인증 Cookie 설정과 일치하도록 CSRF Cookie 속성을 구성한다.
     *
     * @author SeungHyeon.Kang
     * @param cookie CSRF Cookie 응답 속성 Builder
     */
    private void uptCsrfCookie(ResponseCookie.ResponseCookieBuilder cookie) {
        // JavaScript가 Cookie 원문을 읽지 못하게 하고 HTTPS 및 SameSite 전송 정책을 인증 Cookie와 맞춘다
        cookie.httpOnly(true).secure(cookieSecure).sameSite(cookieSameSite).path("/");
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 세부 정책 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 프런트엔드 출처에 허용할 CORS 정책을 담을 객체를 생성한다
        CorsConfiguration config = new CorsConfiguration();

        // AllowedOrigins 업무 값을 config DTO에 설정한다
        config.setAllowedOrigins(List.of(FRONT_DOMAIN)); // 지정된 프론트엔드 도메인만 접근 허용
        // AllowedMethods 업무 값을 config DTO에 설정한다
        config.setAllowedMethods(List.of("*"));           // 모든 HTTP Method 허용
        // AllowedHeaders 업무 값을 config DTO에 설정한다
        config.setAllowedHeaders(List.of("*"));           // 모든 헤더 허용
        // AllowCredentials 업무 값을 config DTO에 설정한다
        config.setAllowCredentials(true);                 // 자격 증명(쿠키, Authorization 헤더 등) 허용

        // 요청 경로별 CORS 정책을 담을 객체를 생성한다
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // API 전체 경로에 CORS 정책을 등록한다
        source.registerCorsConfiguration("/**", config);   // 전체 경로에 CORS 정책 적용
        // CORS(Cross-Origin Resource Sharing) 세부 정책 설정 결과를 반환한다
        return source;
    }
}
