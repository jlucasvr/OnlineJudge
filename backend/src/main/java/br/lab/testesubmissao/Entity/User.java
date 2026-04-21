package br.lab.testesubmissao.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

// ✅ CORRIGIDO: @Table(name = "user") — compatível com o SQL
// ⚠️  ATENÇÃO: "user" é palavra reservada no PostgreSQL.
//     O SQL usa aspas duplas ("user"), o Hibernate também enviará com aspas
//     por causa do dialeto do PostgreSQL — isso funciona corretamente.
@Entity
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue   // ✅ ADICIONADO: faltava @GeneratedValue para o UUID ser gerado automaticamente
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, length = 20)
    private String role = "contestant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public User() {}

    public User(UUID id, String username, String email, String passwordHash, LocalDateTime createdAt, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.role = role;
    }
}
