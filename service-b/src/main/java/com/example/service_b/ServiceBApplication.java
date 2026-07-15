package com.example.service_b;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point Service B (Order Service).
 * Berjalan di port 8082 sebagai backend mock untuk data pesanan.
 */
@SpringBootApplication
public class ServiceBApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceBApplication.class, args);
	}

}
