package com.splitforlater.settlementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.splitforlater.settlementservice",
		"com.splitforlater.common"
})
public class SettlementserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SettlementserviceApplication.class, args);
	}

}
