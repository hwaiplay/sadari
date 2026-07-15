package org.our.sadari.global.common.config;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Web MVC CORS 정책과 정적 리소스 접근 경로를 설정합니다.
 *
 * @author Seunghyeon.Kang
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Value("${domain.front}")
    private String FRONT_DOMAIN;

    /**
     * 프론트 도메인에서 백엔드 API를 호출할 수 있도록 CORS 정책을 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param registry CORS 매핑 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(FRONT_DOMAIN)
                .allowedMethods("*")
                .allowCredentials(true);
    }

    /**
     * 업로드 파일을 브라우저에서 접근할 수 있도록 정적 리소스 핸들러를 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param registry 정적 리소스 핸들러 레지스트리
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // DB에는 파일 시스템 절대 경로가 아닌 /uploads 하위 접근 URL만 저장하므로 여기서 실제 디렉터리를 연결합니다.
        String uploadPath = Paths.get("uploads").toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
