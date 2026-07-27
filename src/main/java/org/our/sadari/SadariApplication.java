package org.our.sadari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Sadari 백엔드 애플리케이션의 실행 진입점입니다.
 * 별도 SchedulerConfig 없이도 @Scheduled 메서드가 동작하도록 이 전역 설정에서 스케줄링을 활성화합니다.
 *
 * @author Seunghyeon.Kang
 */
@EnableRedisHttpSession
@EnableScheduling
@SpringBootApplication
public class SadariApplication {

	/**
	 * Spring Boot 애플리케이션을 실행합니다.
	 *
	 * @author Seunghyeon.Kang
	 * @param args 애플리케이션 실행 인자
	 */
	public static void main(String[] args) {
		SpringApplication.run(SadariApplication.class, args);
	}

}
