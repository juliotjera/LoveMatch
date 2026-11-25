package com.esmia.lovematch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoveMatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoveMatchApplication.class, args);
	}
}
