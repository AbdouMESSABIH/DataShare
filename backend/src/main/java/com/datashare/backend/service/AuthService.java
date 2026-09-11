package com.datashare.backend.service;

import com.datashare.backend.dto.RegisterRequest;
import com.datashare.backend.entity.User;
import com.datashare.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.datashare.backend.dto.LoginRequest;
import com.datashare.backend.security.JwtService;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cet email est déjà utilisé"
            );
        }

        String passwordHash =
                passwordEncoder.encode(request.getPassword());

        User user = new User(email, passwordHash);

        userRepository.save(user);
    }

    public String login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Email ou mot de passe incorrect"
                ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Email ou mot de passe incorrect"
            );
        }

        return jwtService.generateToken(user.getEmail());
    }
}