package com.anhvt.epms.procurement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ProcurementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcurementSystemApplication.class, args);
	}

}
