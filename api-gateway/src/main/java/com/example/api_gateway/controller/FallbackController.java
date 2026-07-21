package com.example.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * FallbackController — Dipanggil otomatis oleh CircuitBreaker filter (Route 1 & Route 2)
 * ketika service-a atau service-b tidak bisa dihubungi (down, timeout, error).
 *
 * <p><strong>Kelompok 7 | Anggota 3</strong></p>
 * Tanpa fallback ini, client akan menerima 500/timeout mentah dari gateway.
 * Dengan fallback, client menerima response yang rapi (503) yang menjelaskan
 * bahwa service tujuan sedang tidak tersedia.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /** Dipanggil ketika Route 1 (user-service-route) circuit breaker terbuka. */
    @GetMapping("/users")
    public ResponseEntity<Object> userServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "SERVICE_UNAVAILABLE",
                "service", "service-a (User Service)",
                "message", "User Service sedang tidak dapat dihubungi. Silakan coba lagi nanti."
        ));
    }

    /** Dipanggil ketika Route 2 (order-service-route) circuit breaker terbuka. */
    @GetMapping("/orders")
    public ResponseEntity<Object> orderServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "SERVICE_UNAVAILABLE",
                "service", "service-b (Order Service)",
                "message", "Order Service sedang tidak dapat dihubungi. Silakan coba lagi nanti."
        ));
    }
}
