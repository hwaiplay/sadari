package org.our.sadari;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "org.our.sadari.**.mapper")
public class SadariApplication {

	public static void main(String[] args) {
		SpringApplication.run(SadariApplication.class, args);
	}

}
