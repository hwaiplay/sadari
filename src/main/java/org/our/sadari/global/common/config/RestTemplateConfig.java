package org.our.sadari.global.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 호출한 계층에서 사용할 처리 결과를 반환한다.
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        // 호출한 계층에서 사용할 처리 결과를 반환한다.
        return new ObjectMapper();
    }
}
