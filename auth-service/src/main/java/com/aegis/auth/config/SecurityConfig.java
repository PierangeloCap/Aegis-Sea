package com.aegis.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean PasswordEncoder — usato da AuthService per:
     * - encode(rawPassword) → genera BCrypt hash alla registrazione
     * - matches(raw, encoded) → verifica la password al login
     *
     * BCrypt con costo 10 (default): ~100ms per hash → sicuro contro brute force.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain — definisce le regole di accesso HTTP.
     *
     * L'auth-service è un servizio INTERNO: non valida JWT in ingresso,
     * li EMETTE. La protezione JWT viene applicata nell'API Gateway.
     * Qui proteggiamo solo contro accessi non autorizzati agli endpoint
     * che non siano register/login.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disabilita CSRF: le API REST stateless non usano cookie di sessione,
                // quindi non sono vulnerabili a CSRF. Abilitarlo causerebbe errori 403
                // su tutte le richieste POST senza token CSRF.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Disabilita HTTP Basic authentication (il popup del browser).
                // Non vogliamo nessuna autenticazione di default da Spring Security.
                .httpBasic(AbstractHttpConfigurer::disable)

                // 3. Regole di autorizzazione per gli endpoint:
                .authorizeHttpRequests(auth -> auth
                        // Endpoint pubblici: register e login non richiedono autenticazione
                        .requestMatchers("/api/auth/**").permitAll()
                        // Actuator health: pubblico per il widget del frontend
                        .requestMatchers("/actuator/**").permitAll()
                        // Tutto il resto richiede autenticazione
                        .anyRequest().authenticated())

                // 4. Sessione STATELESS: Spring Security non crea né usa HttpSession.
                // Ogni richiesta è indipendente — lo stato è nel JWT, non sul server.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}