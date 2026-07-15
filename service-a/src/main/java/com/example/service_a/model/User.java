package com.example.service_a.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User — Entity JPA yang dipetakan ke tabel 'users' di PostgreSQL.
 *
 * Anotasi JPA yang digunakan:
 *   @Entity  : Menandai kelas ini sebagai entitas database (Hibernate akan mengelolanya)
 *   @Table   : Menentukan nama tabel di database (default = nama kelas lowercase)
 *   @Id      : Menandai field ini sebagai Primary Key
 *   @GeneratedValue : Nilai ID dibuat otomatis oleh database (AUTO INCREMENT)
 *   @Column  : Mengatur properti kolom (nama, nullable, length, dll)
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Primary Key — ID unik setiap user.
     * IDENTITY strategy: menggunakan fitur auto-increment bawaan PostgreSQL (SERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nama lengkap user.
     * nullable=false → kolom ini wajib diisi (NOT NULL di database).
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Email user — harus unik di seluruh tabel.
     * unique=true → PostgreSQL akan membuat UNIQUE constraint pada kolom ini.
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Role/peran user (contoh: ADMIN, USER, MODERATOR).
     */
    @Column(name = "role", nullable = false, length = 50)
    private String role;

    // Constructor default — WAJIB ada untuk JPA/Hibernate
    public User() {}

    // Constructor parameterisasi untuk kemudahan pembuatan objek
    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
