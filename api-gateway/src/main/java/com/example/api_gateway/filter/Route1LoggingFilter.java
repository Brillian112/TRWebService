package com.example.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class Route1LoggingFilter
        extends AbstractGatewayFilterFactory<Route1LoggingFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(Route1LoggingFilter.class);

    private static final String HEADER_GATEWAY_SOURCE = "X-Gateway-Source";
    private static final String HEADER_REQUEST_START  = "X-Request-Start";
    private static final String HEADER_RESPONSE_TIME  = "X-Response-Time";
    private static final String HEADER_ROUTE_NAME     = "X-Route-Name";
    private static final String HEADER_FORWARDED_FOR  = "X-Forwarded-For";

    private static final String GATEWAY_NAME  = "api-gateway";
    private static final String ROUTE_1_NAME  = "user-service-route";

    public Route1LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest  request  = exchange.getRequest();
            String             method   = request.getMethod().name();
            String             path     = request.getPath().value();
            String             clientIp = getClientIp(request);
            long               startMs  = System.currentTimeMillis();

            log.info("[ROUTE-1][PRE ] ▶ {} {} | Client: {} | RouteID: {}",
                    method, path, clientIp, ROUTE_1_NAME);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(HEADER_GATEWAY_SOURCE, GATEWAY_NAME)
                    .header(HEADER_REQUEST_START, String.valueOf(startMs))
                    .header(HEADER_ROUTE_NAME, ROUTE_1_NAME)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            boolean isWriteMethod = "POST".equals(method) || "PUT".equals(method);
            if (isWriteMethod) {
                String contentType = request.getHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.contains("application/json")) {
                    log.warn("[ROUTE-1][PRE ] ✗ {} {} ditolak: Content-Type bukan application/json (diterima: {})",
                            method, path, contentType);

                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                    return response.setComplete();
                }
            }

            return chain.filter(mutatedExchange)
                    .then(Mono.fromRunnable(() -> {

                        ServerHttpResponse response   = mutatedExchange.getResponse();
                        long               endMs      = System.currentTimeMillis();
                        long               latencyMs  = endMs - startMs;
                        HttpStatus         status     = (HttpStatus) response.getStatusCode();

                        response.getHeaders().add(HEADER_RESPONSE_TIME, latencyMs + "ms");
                        response.getHeaders().add(HEADER_ROUTE_NAME,    ROUTE_1_NAME);
                        response.getHeaders().add(HEADER_GATEWAY_SOURCE, GATEWAY_NAME);

                        log.info("[ROUTE-1][POST] ◀ {} {} | Status: {} | Latency: {}ms",
                                method, path,
                                status != null ? status.value() : "???",
                                latencyMs);
                    }));
        };
    }

    private String getClientIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst(HEADER_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    public static class Config {
        // Kosong — bisa dikembangkan untuk konfigurasi dinamis jika diperlukan
    }
}
