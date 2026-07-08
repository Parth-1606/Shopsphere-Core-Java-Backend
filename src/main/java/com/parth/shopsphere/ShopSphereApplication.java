package com.parth.shopsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ShopSphereApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopSphereApplication.class, args);
	}

}
