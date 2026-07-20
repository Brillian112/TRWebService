package com.example.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.example.api_gateway.util.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGlobalFilter.class);

    private static final String AUTH_HEADER   = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER      = "X-User";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /** Path yang tidak memerlukan token JWT. */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/actuator",
            "/fallback"
    );

    private final JwtUtil jwtUtil;

    public JwtAuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public int getOrder() {
        // Jalan paling awal, sebelum filter global/route lainnya.
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("[JWT-FILTER] ✗ {} {} ditolak: header Authorization tidak ada / format salah", request.getMethod(), path);
            return rejectUnauthorized(exchange, "Header Authorization: Bearer <token> wajib disertakan");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Claims claims = jwtUtil.validateAndGetClaims(token);

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            log.info("[JWT-FILTER] ✓ {} {} | User: {} | Role: {}", request.getMethod(), path, username, role);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(HEADER_USER, username)
                    .header(HEADER_USER_ROLE, role != null ? role : "")
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT-FILTER] ✗ {} {} ditolak: token tidak valid ({})", request.getMethod(), path, e.getMessage());
            return rejectUnauthorized(exchange, "Token JWT tidak valid atau sudah kedaluwarsa");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> rejectUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"error\":\"UNAUTHORIZED\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}
