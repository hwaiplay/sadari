package org.our.sadari.global.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * fileName       : AsyncExecutionConfigTest
 * author         : SeungHyeon.Kang
 * date           : 2026-11-23
 * description    : 실제 운영 설정의 비동기 실행기 상한과 접수 거절 정책 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-23        SeungHyeon.Kang    최초 생성
 */
class AsyncExecutionConfigTest {

    /**
     * 운영 YML을 실제 Boot 자동 구성에 바인딩하여 스레드와 대기열의 상한 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void productionExecutorBounds() {
        // 비밀 설정을 출력하지 않고 운영 YML의 실행기 설정 해석
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        // 패키징되는 실제 운영 설정 리소스 선택
        yaml.setResources(new ClassPathResource("application-prod.yml"));
        // 운영 설정을 검증할 프로퍼티로 변환
        Properties properties = yaml.getObject();
        // 설정 리소스 누락 방지
        assertNotNull(properties);
        // 실제 Boot 실행기 자동 구성만 구동하여 DB와 외부 API 연결 제외
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
                .withInitializer(context -> {
                    // 기본값과 환경변수 Placeholder가 있는 실제 운영 설정 적용
                    context.getEnvironment().getPropertySources().addFirst(new PropertiesPropertySource("production", properties));
                })
                .run(context -> {
                    // Async가 기본으로 사용하는 실제 실행기 조회
                    ThreadPoolTaskExecutor executor = context.getBean("applicationTaskExecutor", ThreadPoolTaskExecutor.class);
                    // 기본 처리 스레드 상한 확인
                    assertEquals(4, executor.getCorePoolSize());
                    // 대기열 포화 이후에도 무제한 스레드 생성 방지 확인
                    assertEquals(8, executor.getMaxPoolSize());
                    // 무제한 작업 적재 방지 확인
                    assertEquals(100, executor.getThreadPoolExecutor().getQueue().remainingCapacity());
                    // 포화 시 HTTP 요청 스레드에서 대신 실행하지 않는 정책 확인
                    assertInstanceOf(ThreadPoolExecutor.AbortPolicy.class, executor.getThreadPoolExecutor().getRejectedExecutionHandler());
                });
    }
}
