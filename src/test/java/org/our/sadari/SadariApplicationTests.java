package org.our.sadari;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * fileName       : SadariApplicationTests
 * author         : SeungHyeon.Kang
 * date           : 2026-03-01
 * description    : 애플리케이션 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-01        SeungHyeon.Kang    최초 생성
 */
@SpringBootTest(properties = "scheduler.enabled=false")
@ActiveProfiles("loc")
class SadariApplicationTests {

	@Test
	void contextLoads() {
		// 테스트 실행 여부를 콘솔에서 확인한다
		System.out.println("Hello World");
	}

}
