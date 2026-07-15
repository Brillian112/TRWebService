package com.example.service_a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point Service A (User Service).
 * Berjalan di port 8081 sebagai backend mock untuk data pengguna.
 */
@SpringBootApplication
public class ServiceAApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceAApplication.class, args);
	}

}
