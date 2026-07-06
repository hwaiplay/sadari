package org.our.sadari.global.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
/**
 * packageName    : 
 * fileName       : Webconfig.java
 * author         : hanwon.Jang
 * date           : 2026-03-23
 * description    : CORS 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-23       hanwon.Jang       최초 생성
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Value("${domain.front}")
    private String FRONT_DOMAIN; //네이버 앱 시크릿 키

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(FRONT_DOMAIN)
                .allowedMethods("*")
                .allowCredentials(true);
    }
}