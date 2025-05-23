package com.saveetha.LeaveManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication
public class
LeaveManagemnetApplication {


	public static void main(String[] args) {
		SpringApplication.run(LeaveManagemnetApplication.class, args);
		System.out.println("hello yoga");
	}

}
