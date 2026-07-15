package com.example.service_b.repository;

import com.example.service_b.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderRepository — Interface untuk akses data tabel 'orders' di PostgreSQL.
 *
 * Meng-extend JpaRepository<Order, Long> sehingga Spring Data JPA
 * otomatis menyediakan implementasi CRUD lengkap.
 *
 * Generic parameter:
 *   JpaRepository<Order, Long>
 *                  ↑      ↑
 *                Entity   Tipe Primary Key
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Cari semua order milik user tertentu berdasarkan userId.
     * Spring Data JPA menerjemahkan ini menjadi:
     *   SELECT * FROM orders WHERE user_id = ?
     *
     * Berguna untuk menampilkan riwayat pesanan per user.
     */
    List<Order> findByUserId(Long userId);

    /**
     * Cari order berdasarkan status tertentu.
     * Contoh: findByStatus("PENDING") → semua order yang belum diproses
     */
    List<Order> findByStatus(String status);
}
