package org.our.sadari.global.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
/**
 * fileName       : RestTemplateConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-07-06
 * description    : 공통 실행 설정을 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-06        SeungHyeon.Kang    최초 생성
 */
@Configuration
public class RestTemplateConfig {
    /**
     * 외부 HTTP API 호출에 사용할 RestTemplate Bean을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 구성하거나 조회한 결과 객체
     */
    @Bean
    public RestTemplate restTemplate() {
        // 새로 생성한 RestTemplate 객체를 반환한다
        return new RestTemplate();
    }
    /**
     * JSON 직렬화와 역직렬화에 사용할 ObjectMapper Bean을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 구성하거나 조회한 결과 객체
     */
    @Bean
    public ObjectMapper objectMapper() {
        // 새로 생성한 ObjectMapper 객체를 반환한다
        return new ObjectMapper();
    }
}
