package com.syf.economic_simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EconomicSimulatorApplication {

	public static void main(String[] args) {
		System.out.println("DEBUG CHECK: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
		SpringApplication.run(EconomicSimulatorApplication.class, args);
	}

}
