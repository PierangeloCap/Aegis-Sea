package com.aegis.auth.service;

import com.aegis.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;

@Service
public class JwtService {

    // Legge i valori da application.yml
    @Value("${jwt.secret}")
    private String secretHex;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // Converte la stringa HEX in una chiave HMAC-SHA256
    private SecretKey getSigningKey() {
        byte[] keyBytes = HexFormat.of().parseHex(secretHex);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un token JWT per l'utente autenticato.
     * Il token contiene: email (subject), ruolo (claim custom), emissione,
     * scadenza.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail()) // "sub": identifica l'utente
                .claim("role", user.getRole().name()) // "role": OPERATOR / COMMANDER / SUPER_ADMIN
                .claim("userId", user.getId()) // "userId": utile per audit log
                .claim("fullName", user.getFullName()) // "fullName": per la UI
                .issuedAt(new Date()) // "iat": quando è stato emesso
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // "exp"
                .signWith(getSigningKey()) // Firma HMAC-SHA256
                .compact();
    }

    /**
     * Estrae l'email (subject) dal token JWT.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Estrae il ruolo dal claim "role" del token.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Verifica che il token sia valido e non scaduto.
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Token malformato, firma non valida o scaduto
            return false;
        }
    }

    /**
     * Parsing del token: verifica la firma e restituisce tutti i claims.
     * Lancia eccezione se il token è invalido o scaduto.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}