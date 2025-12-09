package com.ak4n1.terra.api.terra_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TerraApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TerraApiApplication.class, args);
	}

}
