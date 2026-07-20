package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * GlobalLoggingFilter — Filter Global yang berjalan untuk SEMUA route.
 *
 * <p><strong>Kelompok 7 | Anggota 2 — Penanganan Filter Route 1</strong></p>
 *
 * <p>
 * Berbeda dengan {@link Route1LoggingFilter} yang spesifik untuk Route 1,
 * filter global ini berjalan di semua route sebelum filter per-route.
 * Berguna untuk:
 * </p>
 *
 * <ul>
 *   <li>Logging request masuk ke gateway (audit trail)</li>
 *   <li>Menambahkan Request ID unik untuk tracing</li>
 *   <li>Monitoring umum tanpa duplikasi kode di setiap route</li>
 * </ul>
 *
 * <h2>Urutan Eksekusi Filter:</h2>
 * <pre>
 *   Request Masuk
 *       │
 *       ▼
 *   [GlobalLoggingFilter.PRE]  ← Filter ini (order -1, jalan pertama)
 *       │
 *       ▼
 *   [Route1LoggingFilter.PRE]  ← Hanya jika cocok Route 1
 *       │
 *       ▼
 *   Service-A (backend)
 *       │
 *       ▼
 *   [Route1LoggingFilter.POST] ← Post-filter Route 1
 *       │
 *       ▼
 *   [GlobalLoggingFilter.POST] ← Filter ini (post, jalan terakhir)
 *       │
 *       ▼
 *   Response ke Client
 * </pre>
 */
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalLoggingFilter.class);

    /**
     * Order -1 memastikan filter ini berjalan SEBELUM filter bawaan Spring Cloud Gateway.
     * Semakin kecil nilainya, semakin awal filter dieksekusi.
     */
    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * Logika filter global — dieksekusi untuk SETIAP request yang masuk ke gateway.
     *
     * @param exchange Objek berisi request dan response yang bisa dimutasi
     * @param chain    Chain filter berikutnya yang akan dieksekusi
     * @return {@link Mono} yang merepresentasikan komputasi async (reactive)
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // =================================================================
        // PRE-PROCESSING — Sebelum request diteruskan ke route manapun
        // =================================================================

        ServerHttpRequest request   = exchange.getRequest();
        String            method    = request.getMethod().name();
        String            path      = request.getPath().value();
        String            requestId = java.util.UUID.randomUUID().toString().substring(0, 8);

        // Log setiap request yang masuk ke gateway
        log.info("[GATEWAY][GLOBAL] ► Incoming: {} {} | ReqID: {}", method, path, requestId);

        // Sisipkan Request ID ke header agar bisa di-trace di semua service
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // =================================================================
        // TERUSKAN ke route filter berikutnya, POST di .then()
        // =================================================================

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(() -> {
                    // POST-PROCESSING: log response yang akan dikembalikan ke client
                    int statusCode = mutatedExchange.getResponse().getStatusCode() != null
                            ? mutatedExchange.getResponse().getStatusCode().value()
                            : 0;

                    log.info("[GATEWAY][GLOBAL] ◄ Outgoing: {} {} | Status: {} | ReqID: {}",
                            method, path, statusCode, requestId);
                }));
    }
}
