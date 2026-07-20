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
}
