package org.our.sadari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableJpaAuditing
@SpringBootApplication
public class SadariApplication {

	public static void main(String[] args) {
		SpringApplication.run(SadariApplication.class, args);
	}

}
