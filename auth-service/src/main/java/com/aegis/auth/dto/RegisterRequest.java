package com.aegis.auth.dto;

import com.aegis.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Il nome è obbligatorio") String fullName,

        @Email(message = "Formato email non valido") @NotBlank(message = "L'email è obbligatoria") String email,

        @NotBlank(message = "La password è obbligatoria") @Size(min = 8, message = "La password deve avere almeno 8 caratteri") String password,

        // Se non specificato, l'utente viene registrato come OPERATOR
        User.Role role) {
    // Record Java 16+: immutabile, con getter/equals/hashCode/toString automatici
    // Perfetto per i DTO di input: non serve mai modificarli dopo la creazione
}