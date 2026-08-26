package org.our.sadari.global.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * fileName       : RestTemplateConfig
 * author         : HanWon.Jang
 * date           : 2026-07-06
 * description    : 공통 실행 설정을 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-06        SeungHyeon.Kang    최초 생성
 * 2026-08-26        HanWon.Jang         외부 HTTP 타임아웃 설정
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 외부 HTTP API 호출에 사용할 RestTemplate Bean을 생성한다
     *
     * @author HanWon.Jang
     * @param connectTimeoutMillis 외부 서버 연결 제한시간(ms)
     * @param readTimeoutMillis 외부 서버 응답 제한시간(ms)
     * @return 구성하거나 조회한 결과 객체
     */
    @Bean
    public RestTemplate restTemplate(
            @Value("${app.http.connect-timeout-millis:3000}") int connectTimeoutMillis
          , @Value("${app.http.read-timeout-millis:5000}") int readTimeoutMillis) {

        // 외부 서버 장애가 요청 스레드를 장시간 점유하지 않도록 연결과 응답 제한시간을 설정한다
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMillis);
        requestFactory.setReadTimeout(readTimeoutMillis);
        // 모든 외부 HTTP 호출이 동일한 제한시간을 사용하도록 공용 클라이언트를 반환한다
        return new RestTemplate(requestFactory);
    }

    /**
     * JSON 직렬화와 역직렬화에 사용할 ObjectMapper Bean을 생성한다
     *
     * @author HanWon.Jang
     * @return 구성하거나 조회한 결과 객체
     */
    @Bean
    public ObjectMapper objectMapper() {

        // 새로 생성한 ObjectMapper 객체를 반환한다
        return new ObjectMapper();
    }
}
