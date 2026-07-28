package org.our.sadari;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
/**
 * fileName       : ServletInitializer
 * author         : SeungHyeon.Kang
 * date           : 2026-03-01
 * description    : 애플리케이션 업무에 필요한 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-01        SeungHyeon.Kang    최초 생성
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// configure 처리 결과를 반환한다
		return application.sources(SadariApplication.class);
	}

}
