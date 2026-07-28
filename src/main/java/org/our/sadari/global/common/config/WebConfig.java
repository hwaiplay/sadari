package org.our.sadari.global.common.config;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * fileName       : WebConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-03-23
 * description    : 공통 실행 설정을 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-23        SeungHyeon.Kang    최초 생성
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // FRONT DOMAIN 설정값
    @Value("${domain.front}")
    private String FRONT_DOMAIN;

    /**
     * 프론트 도메인에서 백엔드 API를 호출할 수 있도록 CORS 정책을 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param registry CORS 매핑 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 프론트엔드 라우팅을 위한 뷰 컨트롤러 경로를 등록한다
        registry.addMapping("/**")
                .allowedOrigins(FRONT_DOMAIN)
                .allowedMethods("*")
                .allowCredentials(true);
    }

    /**
     * 업로드 파일을 브라우저에서 접근할 수 있도록 정적 리소스 핸들러를 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param registry 정적 리소스 핸들러 레지스트리
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // DB에는 파일 시스템 절대 경로가 아닌 /uploads 하위 접근 URL만 저장하므로 여기서 실제 디렉터리를 연결한다.
        String uploadPath = Paths.get("uploads").toAbsolutePath().normalize().toUri().toString();
        // 업로드 파일을 제공할 정적 리소스 경로를 등록한다
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
