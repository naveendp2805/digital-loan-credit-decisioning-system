package com.naveen.credit_decision_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CreditDecisionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreditDecisionServiceApplication.class, args);
	}

}
