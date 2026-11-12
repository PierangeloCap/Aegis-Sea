package com.aegis.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // "user" è parola riservata in PostgreSQL!
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome non può essere vuoto")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Email(message = "Formato email non valido")
    @NotBlank(message = "L'email non può essere vuota")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password; // Sempre BCrypt hash, MAI in chiaro

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Role {
        OPERATOR,    // Accesso in sola lettura alla mappa e agli alert
        COMMANDER,   // Può autorizzare intercettazioni e emettere ordini
        SUPER_ADMIN  // Gestione utenti e configurazioni di sistema
    }
}