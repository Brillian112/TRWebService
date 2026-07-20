                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        package com.example.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ApiGatewayApplication — Entry point aplikasi API Gateway.
 *
 * <p>Kelompok 7 | Topik 4: API Gateway &amp; BFF</p>
 *
 * <p><strong>Arsitektur Sistem:</strong></p>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │                        CLIENT                               │
 * │               (Browser / Postman / Mobile)                  │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │ HTTP Request
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │              API GATEWAY  :8080                             │
 * │          (Spring Cloud Gateway - WebFlux)                   │
 * │                                                             │
 * │  Route 1 [Anggota 2] ──► /api/users/**   ──► service-a     │
 * │  Route 2 [Anggota 3] ──► /api/orders/**  ──► service-b     │
 * └──────────────┬──────────────────────────┬───────────────────┘
 *                │                          │
 *                ▼                          ▼
 * ┌──────────────────────┐    ┌──────────────────────┐
 * │   SERVICE-A  :8081   │    │   SERVICE-B  :8082   │
 * │   (User Service)     │    │   (Order Service)    │
 * │   GET  /api/users    │    │   GET  /api/orders   │
 * │   GET  /api/users/id │    │   POST /api/orders   │
 * │   POST /api/users    │    │   PUT  /api/orders/id│
 * │   PUT  /api/users/id │    └──────────┬───────────┘
 * │   DELETE/api/users/id│               │
 * └──────────┬───────────┘               │
 *            │                           │
 *            └─────────────┬─────────────┘
 *                          ▼
 *              ┌───────────────────────┐
 *              │  PostgreSQL :5432     │
 *              │  Database: webservice_db│
 *              │  Table A: users       │
 *              │  Table B: orders      │
 *              └───────────────────────┘
 * </pre>
 *
 * <p><strong>Cara menjalankan sistem secara bersamaan:</strong></p>
 * <ol>
 *   <li>cd service-a → mvn spring-boot:run  (port 8081)</li>
 *   <li>cd service-b → mvn spring-boot:run  (port 8082)</li>
 *   <li>cd api-gateway → mvn spring-boot:run (port 8080)</li>
 * </ol>
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
