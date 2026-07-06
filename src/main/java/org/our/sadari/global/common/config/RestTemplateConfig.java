package org.our.sadari.global.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 외부 API 호출 객체를 빈으로 관리해 서비스에서 재사용한다.
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        // JSON 변환 객체를 빈으로 관리해 서비스에서 주입받는다.
        return new ObjectMapper();
    }
}
