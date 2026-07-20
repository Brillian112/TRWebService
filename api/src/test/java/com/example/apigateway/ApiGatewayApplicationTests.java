package com.example.apigateway;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

/**
 * ApiGatewayApplicationTests — Test otomatis untuk memverifikasi konfigurasi Route 1.
 *
 * <p><strong>Kelompok 7 | Anggota 2 — Verifikasi Route 1</strong></p>
 *
 * <p>Test ini memverifikasi bahwa:</p>
 * <ol>
 *   <li>Aplikasi gateway berhasil start (context loads)</li>
 *   <li>Route 1 (route-user-service) terdaftar dengan benar</li>
 *   <li>Route 2 (route-order-service) terdaftar sebagai placeholder</li>
 *   <li>Total route yang dikonfigurasi sesuai</li>
 * </ol>
 *
 * <p><strong>Cara menjalankan test:</strong></p>
 * <pre>
 * cd api-gateway
 * mvn test
 * </pre>
 */
@SpringBootTest
class ApiGatewayApplicationTests {

    @Autowired
    private RouteLocator routeLocator;

    // =========================================================================
    // TEST 1 — Context Loads
    // =========================================================================

    /**
     * Verifikasi bahwa Spring Application Context berhasil dimuat.
     * Jika ada kesalahan konfigurasi (missing bean, invalid properties, dll),
     * test ini akan gagal dengan detail error yang jelas.
     */
    @Test
    @DisplayName("Context harus berhasil dimuat tanpa error")
    void contextLoads() {
        // Jika method ini selesai tanpa exception, context berhasil dimuat
        assertThat(routeLocator).isNotNull();
    }

    // =========================================================================
    // TEST 2 — Route 1 Terdaftar
    // =========================================================================

    /**
     * Verifikasi bahwa Route 1 (User Service) terdaftar di gateway.
     * Route ini harus ada agar request /api/users/** bisa diteruskan ke service-a.
     */
    @Test
    @DisplayName("Route 1 (route-user-service) harus terdaftar di gateway")
    void route1UserServiceHarusTerdaftar() {
        // Ambil semua route yang aktif dari RouteLocator
        List<String> routeIds = routeLocator.getRoutes()
                .map(route -> route.getId())
                .collectList()
                .block();

        assertThat(routeIds)
                .isNotNull()
                .isNotEmpty()
                .contains("route-user-service");
    }

    // =========================================================================
    // TEST 3 — Route 2 Terdaftar (Placeholder Anggota 3)
    // =========================================================================

    /**
     * Verifikasi bahwa Route 2 (Order Service) juga terdaftar.
     */
    @Test
    @DisplayName("Route 2 (route-order-service) harus terdaftar sebagai placeholder")
    void route2OrderServiceHarusTerdaftar() {
        List<String> routeIds = routeLocator.getRoutes()
                .map(route -> route.getId())
                .collectList()
                .block();

        assertThat(routeIds)
                .isNotNull()
                .contains("route-order-service");
    }

    // =========================================================================
    // TEST 4 — Jumlah Route
    // =========================================================================

    /**
     * Verifikasi total jumlah route yang terdaftar.
     * Minimal 2 route (Route 1 + Route 2).
     */
    @Test
    @DisplayName("Total route harus minimal 2 (Route 1 + Route 2)")
    void totalRoutHarusMinimal2() {
        long totalRoutes = routeLocator.getRoutes()
                .count()
                .block();

        assertThat(totalRoutes).isGreaterThanOrEqualTo(2L);
    }
}
