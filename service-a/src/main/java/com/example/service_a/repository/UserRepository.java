package com.example.service_a.repository;

import com.example.service_a.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — Interface untuk akses data tabel 'users' di PostgreSQL.
 *
 * Cara kerja:
 *   Dengan meng-extend JpaRepository<User, Long>, Spring Data JPA secara otomatis
 *   (saat startup) membuat implementasi konkret dari interface ini.
 *   Kita TIDAK perlu menulis kode SQL untuk operasi CRUD dasar.
 *
 * Method bawaan JpaRepository yang sudah tersedia:
 *   - findAll()          → SELECT * FROM users
 *   - findById(id)       → SELECT * FROM users WHERE id = ?
 *   - save(user)         → INSERT atau UPDATE
 *   - deleteById(id)     → DELETE FROM users WHERE id = ?
 *   - count()            → SELECT COUNT(*) FROM users
 *   - existsById(id)     → SELECT COUNT(*) > 0 FROM users WHERE id = ?
 *
 * Generic parameter:
 *   JpaRepository<User, Long>
 *                  ↑     ↑
 *                Entity  Tipe Primary Key
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Cari user berdasarkan email.
     * Spring Data JPA otomatis menerjemahkan nama method ini menjadi:
     *   SELECT * FROM users WHERE email = ?
     *
     * Konvensi penamaan: findBy + NamaField
     */
    Optional<User> findByEmail(String email);

    /**
     * Cek apakah email sudah ada di database.
     * Berguna untuk validasi sebelum INSERT data baru.
     */
    boolean existsByEmail(String email);
}
