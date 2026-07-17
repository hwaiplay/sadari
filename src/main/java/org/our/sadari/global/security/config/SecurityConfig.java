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
 * SecurityConfig 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${domain.front}")
    private String FRONT_DOMAIN; // 업무 규칙에서 사용하는 고정 설정 값이다.

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 아래 처리 단계의 업무 목적을 설명한다.
                // 아래 처리 단계의 업무 목적을 설명한다.
                .csrf(csrf -> csrf.disable())

                // 아래 처리 단계의 업무 목적을 설명한다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 아래 처리 단계의 업무 목적을 설명한다.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 아래 처리 단계의 업무 목적을 설명한다.
                // 아래 처리 단계의 업무 목적을 설명한다.
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 아래 처리 단계의 업무 목적을 설명한다.
                .authorizeHttpRequests(auth -> auth

                        // 아래 처리 단계의 업무 목적을 설명한다.
                        .requestMatchers(
                                "/api/oauth/callback/kakao",
                                "/api/oauth/refresh",
                                "/api/oauth/logout",
                                "/api/oauth/tokenCheck",
                                "/uploads/**",
                                // 아래 처리 단계의 업무 목적을 설명한다.
                                "/api/book/search"
                        ).permitAll()

                        
                        // 아래 처리 단계의 업무 목적을 설명한다.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 아래 처리 단계의 업무 목적을 설명한다.
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        // 아래 처리 단계의 업무 목적을 설명한다.
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        // 아래 처리 단계의 업무 목적을 설명한다.
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        })
                )

                // 아래 처리 단계의 업무 목적을 설명한다.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 아래 처리 단계의 업무 목적을 설명한다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(FRONT_DOMAIN));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
