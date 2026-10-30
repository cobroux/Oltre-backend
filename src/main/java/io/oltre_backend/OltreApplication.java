package io.oltre_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OltreApplication {

	public static void main(String[] args) {
		SpringApplication.run(OltreApplication.class, args);
	}

}
