package com.williamm56i.zeke;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ZekeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZekeApplication.class, args);
	}

}
