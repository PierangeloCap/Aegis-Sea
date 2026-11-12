package com.aegis.auth.dto;

import com.aegis.auth.entity.User;

public record AuthResponse(
        String token, // JWT Bearer token
        String email,
        String fullName,
        User.Role role, // OPERATOR / COMMANDER / SUPER_ADMIN
        String tokenType // Sempre "Bearer"
) {
    // Factory method: costruisce la risposta in modo leggibile
    public static AuthResponse of(String token, String email,
            String fullName, User.Role role) {
        return new AuthResponse(token, email, fullName, role, "Bearer");
    }
}