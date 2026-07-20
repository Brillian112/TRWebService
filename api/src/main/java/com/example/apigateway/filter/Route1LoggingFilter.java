package com.example.apigateway.filter;

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

/**
 * Route1LoggingFilter — Custom Filter khusus untuk Route 1 (User Service).
 *
 * <p><strong>Kelompok 7 | Anggota 2 — Penanganan Filter Route 1</strong></p>
 *
 * <p>
 * Filter ini merupakan implementasi {@link GatewayFilter} yang bekerja
 * dalam dua fase untuk setiap request yang melewati Route 1:
 * </p>
 *
 * <h2>Fase PRE (sebelum request diteruskan ke service-a):</h2>
 * <ol>
 *   <li>Logging: catat method, path, client IP, timestamp masuk</li>
 *   <li>Tambahkan header {@code X-Gateway-Source} → service-a tahu via gateway</li>
 *   <li>Tambahkan header {@code X-Request-Start} → timestamp untuk kalkulasi latency</li>
 *   <li>Validasi: cek apakah request body diperlukan (POST/PUT wajib ada Content-Type)</li>
 * </ol>
 *
 * <h2>Fase POST (setelah response diterima dari service-a):</h2>
 * <ol>
 *   <li>Hitung latency = waktu sekarang - X-Request-Start</li>
 *   <li>Tambahkan header {@code X-Response-Time} ke response client</li>
 *   <li>Tambahkan header {@code X-Route-Name} → identifikasi route</li>
 *   <li>Logging: catat status HTTP dan latency</li>
 * </ol>
 *
 * <h2>Cara Daftarkan ke Route:</h2>
 * <pre>
 * // Di GatewayRouteConfig.java:
 * .filters(f -> f.filter(route1LoggingFilter.apply(new Config())))
 * </pre>
 */
@Component
public class Route1LoggingFilter
        extends AbstractGatewayFilterFactory<Route1LoggingFilter.Config> {

    // Logger menggunakan SLF4J (output ke console + log file)
    private static final Logger log = LoggerFactory.getLogger(Route1LoggingFilter.class);

    // Nama header yang ditambahkan ke request/response
    private static final String HEADER_GATEWAY_SOURCE = "X-Gateway-Source";
    private static final String HEADER_REQUEST_START  = "X-Request-Start";
    private static final String HEADER_RESPONSE_TIME  = "X-Response-Time";
    private static final String HEADER_ROUTE_NAME     = "X-Route-Name";
    private static final String HEADER_FORWARDED_FOR  = "X-Forwarded-For";

    // Nilai identitas gateway
    private static final String GATEWAY_NAME  = "api-gateway";
    private static final String ROUTE_1_NAME  = "route-user-service";

    public Route1LoggingFilter() {
        super(Config.class);
    }

    /**
     * Inti logika filter — dieksekusi untuk setiap request yang melewati Route 1.
     *
     * @param config Konfigurasi filter (kosong untuk saat ini, bisa dikembangkan)
     * @return {@link GatewayFilter} yang siap dipakai Spring Cloud Gateway
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // =================================================================
            // FASE PRE — Sebelum request diteruskan ke service-a
            // =================================================================

            ServerHttpRequest  request  = exchange.getRequest();
            String             method   = request.getMethod().name();
            String             path     = request.getPath().value();
            String             clientIp = getClientIp(request);
            long               startMs  = System.currentTimeMillis();

            // ─── PRE LOGGING ────────────────────────────────────────────────
            log.info("[ROUTE-1][PRE ] ▶ {} {} | Client: {} | RouteID: {}",
                    method, path, clientIp, ROUTE_1_NAME);

            // ─── TAMBAHKAN HEADER KE REQUEST ─────────────────────────────────
            //
            // Mutate request: Spring Cloud Gateway menggunakan immutable request,
            // jadi kita harus membuat salinan baru dengan header yang diinginkan.
            //
            ServerHttpRequest mutatedRequest = request.mutate()
                    // Header 1: Identitas sumber request (dari gateway)
                    .header(HEADER_GATEWAY_SOURCE, GATEWAY_NAME)
                    // Header 2: Timestamp mulai untuk kalkulasi latency di fase POST
                    .header(HEADER_REQUEST_START, String.valueOf(startMs))
                    // Header 3: Route yang menangani request ini
                    .header(HEADER_ROUTE_NAME, ROUTE_1_NAME)
                    .build();

            // Buat exchange baru dengan request yang sudah dimutasi
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            // ─── VALIDASI CONTENT-TYPE (POST & PUT) ──────────────────────────
            //
            // Untuk request POST dan PUT, wajib ada Content-Type: application/json.
            // Jika tidak ada, tolak dengan 415 Unsupported Media Type.
            //
            boolean isWriteMethod = "POST".equals(method) || "PUT".equals(method);
            if (isWriteMethod) {
                String contentType = request.getHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.contains("application/json")) {
                    log.warn("[ROUTE-1][PRE ] ✗ {} {} ditolak: Content-Type bukan application/json (diterima: {})",
                            method, path, contentType);

                    // Kembalikan 415 tanpa meneruskan request ke service-a
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                    return response.setComplete();
                }
            }

            // ─── TERUSKAN REQUEST ke service-a, SAMBIL PROSES RESPONSE ───────
            return chain.filter(mutatedExchange)
                    .then(Mono.fromRunnable(() -> {

                        // =============================================================
                        // FASE POST — Setelah response diterima dari service-a
                        // =============================================================

                        ServerHttpResponse response   = mutatedExchange.getResponse();
                        long               endMs      = System.currentTimeMillis();
                        long               latencyMs  = endMs - startMs;
                        HttpStatus         status     = (HttpStatus) response.getStatusCode();

                        // ─── TAMBAHKAN HEADER KE RESPONSE ────────────────────────
                        //
                        // Header ini akan terlihat oleh client (browser/Postman).
                        // Berguna untuk debugging dan monitoring.
                        //
                        response.getHeaders().add(HEADER_RESPONSE_TIME, latencyMs + "ms");
                        response.getHeaders().add(HEADER_ROUTE_NAME,    ROUTE_1_NAME);
                        response.getHeaders().add(HEADER_GATEWAY_SOURCE, GATEWAY_NAME);

                        // ─── POST LOGGING ─────────────────────────────────────────
                        log.info("[ROUTE-1][POST] ◀ {} {} | Status: {} | Latency: {}ms",
                                method, path,
                                status != null ? status.value() : "???",
                                latencyMs);
                    }));
        };
    }

    // =========================================================================
    // HELPER METHOD
    // =========================================================================

    /**
     * Mengambil IP address nyata client.
     *
     * <p>
     * Jika request melalui load balancer atau proxy, IP asli client
     * biasanya ada di header {@code X-Forwarded-For}, bukan di
     * {@code remoteAddress} yang menunjuk ke proxy.
     * </p>
     *
     * @param request HTTP request yang masuk ke gateway
     * @return String representasi IP address client
     */
    private String getClientIp(ServerHttpRequest request) {
        // Cek header X-Forwarded-For terlebih dahulu (jika ada proxy/LB)
        String forwarded = request.getHeaders().getFirst(HEADER_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isEmpty()) {
            // X-Forwarded-For bisa berisi daftar IP, ambil yang pertama (IP asli client)
            return forwarded.split(",")[0].trim();
        }
        // Fallback ke remote address langsung
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    // =========================================================================
    // KELAS CONFIG — Dapat dikembangkan untuk konfigurasi dinamis filter
    // =========================================================================

    /**
     * Config — Kelas konfigurasi untuk filter ini.
     *
     * <p>
     * Saat ini kosong, tapi bisa dikembangkan untuk menerima parameter
     * seperti enableLogging, maxBodySize, allowedRoles, dll.
     * </p>
     *
     * <p>Contoh penggunaan dengan config:</p>
     * <pre>
     * // Di application.properties:
     * spring.cloud.gateway.routes[0].filters[0]=Route1LoggingFilter=enableLogging=true
     * </pre>
     */
    public static class Config {
        // Kosong untuk sekarang — bisa ditambah field konfigurasi jika diperlukan
        // Contoh: private boolean enableDetailedLogging = true;
    }
}
