package com.example.api_gateway.config;

import java.net.URI;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import com.example.api_gateway.filter.ApiKeyCheckGatewayFilterFactory;
import com.example.api_gateway.filter.Route1LoggingFilter;

@Configuration
public class GatewayConfig {

    private static final String SERVICE_A_URI = "http://localhost:8081"; 
    private static final String SERVICE_B_URI = "http://localhost:8082"; 

    private final ApiKeyCheckGatewayFilterFactory apiKeyCheckGatewayFilterFactory;
    private final Route1LoggingFilter route1LoggingFilter;

    public GatewayConfig(ApiKeyCheckGatewayFilterFactory apiKeyCheckGatewayFilterFactory,
                          Route1LoggingFilter route1LoggingFilter) {
        this.apiKeyCheckGatewayFilterFactory = apiKeyCheckGatewayFilterFactory;
        this.route1LoggingFilter = route1LoggingFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                            ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        return builder.routes()

                .route("user-service-route", r -> r
                        .path("/api/users/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .filter(route1LoggingFilter.apply(new Route1LoggingFilter.Config()))
                                .addRequestHeader("X-Api-Version", "v1")
                                .addResponseHeader("X-Served-By", "api-gateway")
                                .circuitBreaker(c -> c
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri(URI.create("forward:/fallback/users")))
                        )
                        .uri(SERVICE_A_URI))

                .route("order-service-route", r -> r
                        .path("/api/orders/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT)
                        .filters(f -> f
                                .filter(apiKeyCheckGatewayFilterFactory.apply(new ApiKeyCheckGatewayFilterFactory.Config()))
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addResponseHeader("X-Response-From", "service-b")
                                .circuitBreaker(c -> c
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri(URI.create("forward:/fallback/orders")))
                        )
                        .uri(SERVICE_B_URI))

                .build();
    }

/**
 * GatewayConfig — Definisi seluruh route API Gateway.
 *
 * Setiap route menentukan 4 hal:
 *   1. Predicate PATH     -> pola URL yang akan ditangkap gateway
 *   2. Predicate METHOD   -> HTTP method yang diizinkan untuk route tsb
 *   3. Target (URI)       -> alamat service tujuan (downstream)
 *   4. Filters            -> transformasi/validasi request-response sebelum
 *                            diteruskan atau sebelum dikembalikan ke client
 */
@Configuration
public class GatewayConfig {

    private static final String SERVICE_A_URI = "http://localhost:8081"; // User Service
    private static final String SERVICE_B_URI = "http://localhost:8082"; // Order Service

        private final ApiKeyCheckGatewayFilterFactory apiKeyCheckGatewayFilterFactory;

        public GatewayConfig(ApiKeyCheckGatewayFilterFactory apiKeyCheckGatewayFilterFactory) {
                this.apiKeyCheckGatewayFilterFactory = apiKeyCheckGatewayFilterFactory;
        }

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                                ReactiveCircuitBreakerFactory circuitBreakerFactory) {
                return builder.routes()

                        // =========================================================================
                        // ROUTE 1 — USER SERVICE (service-a)
                        // -------------------------------------------------------------------------
                        // Path    : /api/users/**
                        // Method  : GET, POST, PUT, DELETE (semua method CRUD user diizinkan)
                        // Target  : http://localhost:8081  (service-a)
                        // Filter  :
                        //   - AddRequestHeader  : menyisipkan header X-Gateway-Source=api-gateway
                        //                         supaya service-a tahu request datang lewat gateway
                        //   - AddResponseHeader : menandai response dengan X-Response-From=service-a
                        //   - CircuitBreaker    : jika service-a down/timeout, request dialihkan
                        //                         ke fallback (GET /fallback/users) alih-alih hang/500
                        //   - LoggingGlobalFilter (global, otomatis berlaku juga di sini)
                        // =========================================================================
                        .route("user-service-route", r -> r
                                .path("/api/users/**")
                                .and()
                                .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                                .filters(f -> f
                                        .addRequestHeader("X-Gateway-Source", "api-gateway")
                                        .addResponseHeader("X-Response-From", "service-a")
                                        .circuitBreaker(c -> c
                                                .setName("userServiceCircuitBreaker")
                                                .setFallbackUri(URI.create("forward:/fallback/users")))
                                )
                                .uri(SERVICE_A_URI))

                        // =========================================================================
                        // ROUTE 2 — ORDER SERVICE (service-b)
                        // -------------------------------------------------------------------------
                        // Path    : /api/orders/**
                        // Method  : GET, POST, PUT (order tidak boleh DELETE lewat gateway ini,
                        //           sengaja dibatasi untuk mencegah penghapusan data transaksi)
                        // Target  : http://localhost:8082  (service-b)
                        // Filter  :
                        //   - ApiKeyCheck       : filter custom, mewajibkan header X-API-KEY yang
                        //                         valid untuk request POST/PUT (write operation)
                        //   - AddRequestHeader  : menyisipkan header X-Gateway-Source=api-gateway
                        //   - AddResponseHeader : menandai response dengan X-Response-From=service-b
                        //   - CircuitBreaker    : jika service-b down/timeout, request dialihkan
                        //                         ke fallback (GET /fallback/orders)
                        //   - LoggingGlobalFilter (global, otomatis berlaku juga di sini)
                        // =========================================================================
                        .route("order-service-route", r -> r
                                .path("/api/orders/**")
                                .and()
                                .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT)
                                .filters(f -> f
                                        .filter(apiKeyCheckGatewayFilterFactory.apply(new ApiKeyCheckGatewayFilterFactory.Config()))
                                        .addRequestHeader("X-Gateway-Source", "api-gateway")
                                        .addResponseHeader("X-Response-From", "service-b")
                                        .circuitBreaker(c -> c
                                                .setName("orderServiceCircuitBreaker")
                                                .setFallbackUri(URI.create("forward:/fallback/orders")))
                                )
                                .uri(SERVICE_B_URI))

                        .build();
        }
}
