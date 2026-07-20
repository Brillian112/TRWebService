package com.example.service_a.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service_a.model.User;
import com.example.service_a.repository.UserRepository;

/**
 * UserController — REST Controller untuk Service A (User Service).
 *
 * Menyediakan operasi CRUD lengkap:
 *   GET    /api/users          → ambil semua user
 *   GET    /api/users/{id}     → ambil user by ID
 *   POST   /api/users          → buat user baru
 *   PUT    /api/users/{id}     → update user
 *   DELETE /api/users/{id}     → hapus user
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================================================================
    // READ — Ambil Data
    // =========================================================================

    /**
     * GET /api/users
     * Mengambil semua user dari database.
     * Query: SELECT * FROM users
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers() {
        List<User> users = userRepository.findAll();

        return ResponseEntity.ok(Map.of(
            "service",     "service-a",
            "description", "User Service",
            "total",       users.size(),
            "data",        users
        ));
    }

    /**
     * GET /api/users/{id}
     * Mengambil satu user berdasarkan ID.
     * Query: SELECT * FROM users WHERE id = ?
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(404).body(Map.of(
            "error",   "NOT_FOUND",
            "message", "User dengan ID " + id + " tidak ditemukan"
        ));
    }

    // =========================================================================
    // CREATE — Tambah Data Baru
    // =========================================================================

    /**
     * POST /api/users
     * Menyimpan user baru ke database.
     *
     * Request body (JSON):
     *   { "name": "John Doe", "email": "john@example.com", "role": "USER" }
     *
     * Response: 201 Created + objek user yang baru tersimpan (termasuk ID)
     */
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        // Validasi: cek email duplikat sebelum insert
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(409).body(Map.of(
                "error",   "CONFLICT",
                "message", "Email '" + user.getEmail() + "' sudah terdaftar"
            ));
        }

        // INSERT INTO users (name, email, role) VALUES (?, ?, ?)
        User saved = userRepository.save(user);
        return ResponseEntity.status(201).body(saved);
    }

    // =========================================================================
    // UPDATE — Perbarui Data yang Ada
    // =========================================================================

    /**
     * PUT /api/users/{id}
     * Memperbarui data user yang sudah ada (full update / replace).
     *
     * Cara kerja PUT:
     *   1. Cari user berdasarkan ID → jika tidak ada, kembalikan 404
     *   2. Terapkan semua field dari request body ke entitas yang ada
     *   3. Simpan kembali (save() → UPDATE SQL)
     *
     * Request body (JSON) — semua field wajib diisi:
     *   { "name": "John Updated", "email": "john@example.com", "role": "ADMIN" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody User updatedData) {
        // Langkah 1: Cari user yang akan diupdate
        Optional<User> existing = userRepository.findById(id);

        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                "error",   "NOT_FOUND",
                "message", "User dengan ID " + id + " tidak ditemukan"
            ));
        }

        // Langkah 2: Update field-field pada entitas yang sudah ada
        User user = existing.get();
        user.setName(updatedData.getName());
        user.setRole(updatedData.getRole());

        // Update email hanya jika berbeda dengan email saat ini
        // (mencegah konflik dengan email user lain)
        if (!user.getEmail().equals(updatedData.getEmail())) {
            if (userRepository.existsByEmail(updatedData.getEmail())) {
                return ResponseEntity.status(409).body(Map.of(
                    "error",   "CONFLICT",
                    "message", "Email '" + updatedData.getEmail() + "' sudah dipakai user lain"
                ));
            }
            user.setEmail(updatedData.getEmail());
        }

        // Langkah 3: Simpan → UPDATE users SET name=?, email=?, role=? WHERE id=?
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    // =========================================================================
    // DELETE — Hapus Data
    // =========================================================================

    /**
     * DELETE /api/users/{id}
     * Menghapus user berdasarkan ID.
     *
     * Cara kerja:
     *   1. Cek apakah user dengan ID tersebut ada
     *   2. Jika ada → DELETE FROM users WHERE id = ?
     *   3. Jika tidak → kembalikan 404
     *
     * Response: 200 OK dengan pesan konfirmasi (bukan 204 agar mudah dibaca)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        // Cek keberadaan user sebelum menghapus
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of(
                "error",   "NOT_FOUND",
                "message", "User dengan ID " + id + " tidak ditemukan"
            ));
        }

        // DELETE FROM users WHERE id = ?
        userRepository.deleteById(id);

        return ResponseEntity.ok(Map.of(
            "message", "User dengan ID " + id + " berhasil dihapus"
        ));
    }
}
