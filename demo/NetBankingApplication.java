package com.cmrit.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cmrit.demo")
//@SpringBootApplication
public class NetBankingApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetBankingApplication.class, args);
	}

}
