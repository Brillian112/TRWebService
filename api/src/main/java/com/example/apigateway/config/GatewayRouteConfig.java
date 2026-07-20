package com.example.apigateway.config;

import com.example.apigateway.filter.Route1LoggingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * GatewayRouteConfig — Konfigurasi Route API Gateway berbasis Java DSL.
 *
 * <p><strong>Kelompok 7 | Anggota 2 — API Gateway Route 1</strong></p>
 *
 * <p>
 * Kelas ini mendefinisikan route secara programatik menggunakan
 * {@link RouteLocatorBuilder} yang disediakan Spring Cloud Gateway.
 * Pendekatan Java DSL ini merupakan alternatif dari konfigurasi
 * {@code application.properties} dan lebih fleksibel karena bisa
 * menggunakan logika Java seperti kondisi if, variabel, dll.
 * </p>
 *
 * <h2>Komponen Route:</h2>
 * <ul>
 *   <li><strong>Path (Predicate)</strong>   : Pattern URL yang ditangkap gateway</li>
 *   <li><strong>Method (Predicate)</strong> : HTTP method yang diizinkan</li>
 *   <li><strong>Target (URI)</strong>       : Alamat backend service tujuan</li>
 *   <li><strong>Filters</strong>            : Transformasi yang diterapkan</li>
 * </ul>
 *
 * <h2>Route yang Dikonfigurasi Anggota 2:</h2>
 * <pre>
 *   Route ID      : route-user-service
 *   Path Predicate: /api/users/**
 *   Method        : GET, POST, PUT, DELETE
 *   Target URI    : http://localhost:8081
 *   Filters       : AddRequestHeader, AddResponseHeader, Route1LoggingFilter
 * </pre>
 */
@Configuration
public class GatewayRouteConfig {

    // =====================================================================
    // KONSTANTA TARGET SERVICE
    // =====================================================================

    /**
     * URL base service-a (User Service) — dikerjakan Anggota 1 sebagai mock.
     * Service-a menjalankan endpoint /api/users di port 8081.
     */
    private static final String USER_SERVICE_URL  = "http://localhost:8081";

    /**
     * URL base service-b (Order Service).
     * Route 2 dikonfigurasi oleh Anggota 3.
     */
    private static final String ORDER_SERVICE_URL = "http://localhost:8082";

    // =====================================================================
    // DEPENDENCY INJECTION
    // =====================================================================

    /** Filter custom khusus Route 1 yang bertanggung jawab atas logging dan header. */
    private final Route1LoggingFilter route1LoggingFilter;

    public GatewayRouteConfig(Route1LoggingFilter route1LoggingFilter) {
        this.route1LoggingFilter = route1LoggingFilter;
    }

    // =====================================================================
    // BEAN ROUTE LOCATOR
    // =====================================================================

    /**
     * Mendefinisikan semua route API Gateway.
     *
     * <p>Spring Cloud Gateway mencocokkan setiap request yang masuk
     * dengan daftar route secara berurutan — route pertama yang predicatenya
     * cocok akan digunakan (first-match wins).</p>
     *
     * @param builder {@link RouteLocatorBuilder} yang di-inject oleh Spring
     * @return {@link RouteLocator} berisi semua definisi route aktif
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ===========================================================
                // ░░ ROUTE 1 — USER SERVICE ░░
                // [TUGAS ANGGOTA 2]
                // ===========================================================
                //
                //  Semua request ke /api/users/** akan diteruskan ke service-a.
                //
                //  Contoh alur:
                //
                //  Client                  Gateway                 Service-A
                //    │                       │                        │
                //    │ GET /api/users        │                        │
                //    │──────────────────────►│                        │
                //    │                       │  [PRE Filter jalan]    │
                //    │                       │  Tambah header         │
                //    │                       │  Log request           │
                //    │                       │                        │
                //    │                       │ GET /api/users         │
                //    │                       │───────────────────────►│
                //    │                       │                        │ Query DB
                //    │                       │     200 OK + data      │
                //    │                       │◄───────────────────────│
                //    │                       │  [POST Filter jalan]   │
                //    │                       │  Tambah header resp    │
                //    │                       │  Log response          │
                //    │     200 OK + data     │                        │
                //    │◄──────────────────────│                        │
                //
                .route("route-user-service", routeSpec -> routeSpec

                        // ─────────────────────────────────────────────────
                        // PREDICATE 1 — Path Predicate
                        // ─────────────────────────────────────────────────
                        //
                        // Mencocokkan URL path yang dimulai dengan /api/users/
                        // Pattern /** (double asterisk) = wildcard rekursif:
                        //
                        //   /api/users       ✓ cocok (ambil semua user)
                        //   /api/users/1     ✓ cocok (ambil user ID 1)
                        //   /api/users/99    ✓ cocok (ambil user ID 99)
                        //   /api/orders      ✗ tidak cocok
                        //   /users           ✗ tidak cocok (tidak ada prefix /api)
                        //
                        .path("/api/users/**")

                        // ─────────────────────────────────────────────────
                        // PREDICATE 2 — Method Predicate
                        // ─────────────────────────────────────────────────
                        //
                        // Hanya method berikut yang diteruskan ke service-a:
                        //   GET    → Ambil data user (Read)
                        //   POST   → Buat user baru (Create)
                        //   PUT    → Update user (Update)
                        //   DELETE → Hapus user (Delete)
                        //
                        // Method selain ini (misal PATCH, HEAD) akan dijawab
                        // dengan 405 Method Not Allowed.
                        //
                        .and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)

                        // ─────────────────────────────────────────────────
                        // FILTERS — Penanganan Request & Response Route 1
                        // ─────────────────────────────────────────────────
                        .filters(filterSpec -> filterSpec

                                // ── FILTER 1: Custom Logging Filter ───────
                                //
                                // Route1LoggingFilter yang kita buat sendiri.
                                // Menangani:
                                //   [PRE]  - Log request, tambah header X-Gateway-Source,
                                //            X-Request-Start, X-Route-Name
                                //            Validasi Content-Type untuk POST/PUT
                                //   [POST] - Hitung latency, tambah X-Response-Time,
                                //            log response status
                                //
                                .filter(route1LoggingFilter.apply(new Route1LoggingFilter.Config()))

                                // ── FILTER 2: AddRequestHeader ─────────────
                                //
                                // Tambahkan header tambahan ke request sebelum
                                // diteruskan ke service-a.
                                //
                                // Header X-Api-Version berguna untuk service-a
                                // agar bisa melakukan backward compatibility.
                                //
                                .addRequestHeader("X-Api-Version", "v1")

                                // ── FILTER 3: AddResponseHeader ─────────────
                                //
                                // Tambahkan header ke response yang dikembalikan
                                // ke client. Berguna untuk CORS atau metadata.
                                //
                                .addResponseHeader("X-Served-By", "api-gateway")
                        )

                        // ─────────────────────────────────────────────────
                        // TARGET URI — Tujuan akhir request Route 1
                        // ─────────────────────────────────────────────────
                        //
                        // Request yang lolos semua predicate di atas akan
                        // diteruskan ke service-a di alamat ini.
                        //
                        // Path /api/users/** tetap dipertahankan (tidak di-strip).
                        // Tidak ada StripPrefix filter, sehingga:
                        //   Gateway  : GET http://localhost:8080/api/users/5
                        //   Service-A: GET http://localhost:8081/api/users/5
                        //
                        .uri(USER_SERVICE_URL)
                )

                // ===========================================================
                // ░░ ROUTE 2 — ORDER SERVICE ░░
                // [TUGAS ANGGOTA 3 — placeholder, dikerjakan Anggota 3]
                // ===========================================================
                .route("route-order-service", routeSpec -> routeSpec
                        .path("/api/orders/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .uri(ORDER_SERVICE_URL)
                )

                .build();
    }
}
