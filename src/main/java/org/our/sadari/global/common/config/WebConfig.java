package org.our.sadari.global.common.config;

import java.nio.file.Paths;
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

    /**
     * 프로젝트 내부 업로드 디렉터리에 저장한 프로필 이미지를 정적 리소스로 제공한다.
     * @Author Seunghyeon.Kang
     * @param registry 정적 리소스 핸들러 레지스트리
     * @return
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get("uploads").toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
