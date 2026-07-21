package com.example.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point API Gateway.
 * Berjalan di port 8080 sebagai satu-satunya pintu masuk (single entry point)
 * bagi client menuju service-a (User Service, :8081) dan service-b (Order Service, :8082).
 *
 * Client / BFF cukup memanggil:
 *   http://localhost:8080/api/users/**   -> diteruskan ke service-a
 *   http://localhost:8080/api/orders/**  -> diteruskan ke service-b
 */
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
