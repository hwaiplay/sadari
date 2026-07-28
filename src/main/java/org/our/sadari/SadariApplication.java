package org.our.sadari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * fileName       : SadariApplication
 * author         : SeungHyeon.Kang
 * date           : 2026-03-01
 * description    : 애플리케이션 업무에 필요한 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-01        SeungHyeon.Kang    최초 생성
 */
@EnableRedisHttpSession
@EnableScheduling
@SpringBootApplication
public class SadariApplication {

	/**
	 * Spring Boot 애플리케이션을 실행한다.
	 *
	 * @author SeungHyeon.Kang
	 * @param args 애플리케이션 실행 인자
	 */
	public static void main(String[] args) {

		// 검증 대상 작업을 실행한다
		SpringApplication.run(SadariApplication.class, args);
	}

}
