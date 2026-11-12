package com.aegis.auth.service;

import com.aegis.auth.dto.AuthResponse;
import com.aegis.auth.dto.LoginRequest;
import com.aegis.auth.dto.RegisterRequest;
import com.aegis.auth.entity.User;
import com.aegis.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Lombok: inietta tutti i campi final via costruttore
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Bean dichiarato in SecurityConfig
    private final JwtService jwtService;

    /**
     * Registra un nuovo utente nel sistema.
     * - Verifica che l'email non sia già in uso
     * - Hasha la password con BCrypt
     * - Salva l'utente e restituisce un JWT già pronto all'uso
     */
    public AuthResponse register(RegisterRequest request) {
        // 1. Controllo duplicati
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email già registrata: " + request.email());
        }

        // 2. Determina il ruolo (default: OPERATOR se non specificato)
        User.Role role = (request.role() != null) ? request.role() : User.Role.OPERATOR;

        // 3. Costruisce l'entità con Builder pattern (Lombok)
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // BCrypt hash
                .role(role)
                .build();

        // 4. Salva sul database
        User savedUser = userRepository.save(user);

        // 5. Genera e restituisce il JWT
        String token = jwtService.generateToken(savedUser);
        return AuthResponse.of(token, savedUser.getEmail(),
                savedUser.getFullName(), savedUser.getRole());
    }

    /**
     * Autentica un utente esistente.
     * - Cerca l'utente per email
     * - Verifica la password con BCrypt (matches → confronta hash senza decifrare)
     * - Restituisce un nuovo JWT
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Cerca l'utente per email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenziali non valide"));
        // NB: messaggio generico per non rivelare se l'email esiste o no

        // 2. Verifica la password (BCrypt.matches confronta testo con hash)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenziali non valide");
        }

        // 3. Genera e restituisce il JWT
        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user.getEmail(),
                user.getFullName(), user.getRole());
    }
}