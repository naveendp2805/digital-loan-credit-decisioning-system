package com.naveen.repayment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RepaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepaymentServiceApplication.class, args);
	}

}
