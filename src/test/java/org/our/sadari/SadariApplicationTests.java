package org.our.sadari;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 애플리케이션 전체 Spring Context가 정상 생성되는지 검증합니다.
 * 테스트 도중 실제 사용자에게 정기 알림이 발송되지 않도록 스케줄러 실행 빈만 비활성화합니다.
 *
 * @author Seunghyeon.Kang
 */
@SpringBootTest(properties = "scheduler.enabled=false")
@ActiveProfiles("loc")
class SadariApplicationTests {

	@Test
	void contextLoads() {
		System.out.println("Hello World");
	}

}
