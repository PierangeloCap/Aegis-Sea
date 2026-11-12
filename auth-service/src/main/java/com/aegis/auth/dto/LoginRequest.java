package com.aegis.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Email(message = "Formato email non valido") @NotBlank(message = "L'email è obbligatoria") String email,

        @NotBlank(message = "La password è obbligatoria") String password) {
}