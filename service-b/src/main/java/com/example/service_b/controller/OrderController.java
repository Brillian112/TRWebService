package com.example.service_b.controller;

import com.example.service_b.model.Order;
import com.example.service_b.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OrderController — REST Controller untuk Service B (Order Service).
 *
 * Membaca data dari PostgreSQL melalui OrderRepository.
 *
 * Diakses via Gateway: http://localhost:8080/api/orders
 * Langsung ke service: http://localhost:8082/api/orders
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * GET /api/orders
     * Mengambil semua order dari tabel 'orders' di PostgreSQL.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders() {
        // orderRepository.findAll() → SELECT * FROM orders
        List<Order> orders = orderRepository.findAll();

        Map<String, Object> response = Map.of(
            "service",     "service-b",
            "description", "Order Service",
            "total",       orders.size(),
            "data",        orders
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/orders/{id}
     * Mengambil satu order berdasarkan ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        } else {
            return ResponseEntity.status(404).body(Map.of(
                "error",   "NOT_FOUND",
                "message", "Order dengan ID " + id + " tidak ditemukan"
            ));
        }
    }

    /**
     * GET /api/orders/user/{userId}
     * Mengambil semua order milik user tertentu.
     * Query: SELECT * FROM orders WHERE user_id = ?
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "total",  orders.size(),
            "data",   orders
        ));
    }

    /**
     * POST /api/orders
     * Menyimpan order baru ke database.
     *
     * Request body: { "userId": 1, "product": "...", "quantity": 1,
     *                 "totalPrice": 10000, "status": "PENDING" }
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // save() → INSERT INTO orders (...) VALUES (...)
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.status(201).body(savedOrder);
    }

    /**
     * PUT /api/orders/{id}
     * Memperbarui data order yang sudah ada (Update).
     *
     * Request body: { "product": "Baru", "quantity": 2, "totalPrice": 20000, "status": "PROCESSING" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        Optional<Order> orderOptional = orderRepository.findById(id);

        if (orderOptional.isPresent()) {
            Order existingOrder = orderOptional.get();
            
            // Update field yang diperbolehkan
            existingOrder.setProduct(orderDetails.getProduct());
            existingOrder.setQuantity(orderDetails.getQuantity());
            existingOrder.setTotalPrice(orderDetails.getTotalPrice());
            existingOrder.setStatus(orderDetails.getStatus());
            // Note: userId biasanya tidak diubah setelah order dibuat

            // save() akan melakukan UPDATE jika ID sudah ada di database
            Order updatedOrder = orderRepository.save(existingOrder);
            
            return ResponseEntity.ok(updatedOrder);
        } else {
            return ResponseEntity.status(404).body(Map.of(
                "error",   "NOT_FOUND",
                "message", "Order dengan ID " + id + " tidak ditemukan untuk diupdate"
            ));
        }
    }
}
