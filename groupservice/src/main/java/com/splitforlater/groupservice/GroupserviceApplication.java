package com.splitforlater.groupservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.splitforlater.groupservice",
		"com.splitforlater.common"
})
public class GroupserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroupserviceApplication.class, args);
	}

}
