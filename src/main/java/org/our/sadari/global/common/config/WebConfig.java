package org.our.sadari.global.common.config;

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
 * 2026-08-07        SeungHyeon.Kang    업로드 파일 제공을 저장소 컨트롤러로 이관
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
}
