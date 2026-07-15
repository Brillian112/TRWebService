package com.example.service_b.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Order — Entity JPA yang dipetakan ke tabel 'orders' di PostgreSQL.
 *
 * Catatan: 'order' adalah reserved keyword di SQL, maka nama tabel
 * diberi nama 'orders' (plural) untuk menghindari konflik.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Referensi ke ID user pemilik pesanan.
     * Ini adalah relasi logis — bukan @ManyToOne karena Service B
     * tidak punya akses ke entitas User (beda service).
     * Di microservice, relasi antar service cukup simpan ID-nya saja.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product", nullable = false, length = 200)
    private String product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Total harga pesanan.
     * precision=15, scale=2 → mendukung angka seperti 999_999_999_999_999.99
     */
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Status pesanan: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    // Constructor default — WAJIB ada untuk JPA/Hibernate
    public Order() {}

    // Constructor parameterisasi
    public Order(Long userId, String product, Integer quantity, BigDecimal totalPrice, String status) {
        this.userId = userId;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
