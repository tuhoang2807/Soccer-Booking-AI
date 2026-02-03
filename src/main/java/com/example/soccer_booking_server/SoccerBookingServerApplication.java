package com.example.soccer_booking_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class SoccerBookingServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoccerBookingServerApplication.class, args);
	}

}
