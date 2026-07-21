package com.example.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * LoggingGlobalFilter — Filter global (berlaku untuk SEMUA route: Route 1 & Route 2).
 *
 * Tugasnya:
 *   1. PRE  filter : mencatat method, path, dan waktu mulai request MASUK ke gateway.
 *   2. POST filter : mencatat status code response dan durasi (latency) setelah
 *                    request diteruskan (proxy) ke service tujuan dan balik lagi.
 *
 * Implements Ordered agar bisa dipastikan filter ini jalan paling awal (HIGHEST_PRECEDENCE).
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        log.info("[GATEWAY-IN] {} {} dari {}",
                request.getMethod(),
                request.getURI(),
                request.getRemoteAddress());

        // chain.filter(exchange) meneruskan request ke filter berikutnya,
        // lalu akhirnya ke target service sesuai route yang cocok.
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GATEWAY-OUT] {} {} -> status={} ({} ms)",
                    request.getMethod(),
                    request.getURI(),
                    exchange.getResponse().getStatusCode(),
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
