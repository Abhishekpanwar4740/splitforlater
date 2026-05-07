package com.splitforlater.expenseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.splitforlater.expenseservice",
		"com.splitforlater.common"
})
public class ExpenseserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpenseserviceApplication.class, args);
	}

}
