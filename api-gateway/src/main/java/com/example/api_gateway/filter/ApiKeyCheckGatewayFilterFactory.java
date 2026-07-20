package com.example.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyCheckGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ApiKeyCheckGatewayFilterFactory.Config> {

    private static final String REQUIRED_KEY = "secret-order-key";

    public ApiKeyCheckGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String method = request.getMethod() != null ? request.getMethod().name() : "";

            boolean isWriteMethod = method.equals("POST") || method.equals("PUT") || method.equals("DELETE");

            if (isWriteMethod) {
                String apiKey = request.getHeaders().getFirst("X-API-KEY");
                if (apiKey == null || !apiKey.equals(REQUIRED_KEY)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    byte[] body = ("{\"error\":\"UNAUTHORIZED\",\"message\":\"Header X-API-KEY tidak valid untuk mengakses Order Service\"}")
                            .getBytes();
                    org.springframework.core.io.buffer.DataBuffer buffer =
                            exchange.getResponse().bufferFactory().wrap(body);
                    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                }
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
