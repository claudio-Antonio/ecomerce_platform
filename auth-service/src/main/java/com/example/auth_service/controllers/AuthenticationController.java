package com.example.auth_service.controllers;

import com.example.auth_service.controllers.dtos.AuthenticationDTO;
import com.example.auth_service.controllers.dtos.LoginResponseDTO;
import com.example.auth_service.controllers.dtos.RegisterDTO;
import com.example.auth_service.domain.User;
import com.example.auth_service.infra.redis.BlacklistToken;
import com.example.auth_service.infra.security.SecurityConfiguration;
import com.example.auth_service.infra.security.TokenService;
import com.example.auth_service.kafka.producer.UserEventProducer;
import com.example.auth_service.repositories.redis.BlacklistRepository;
import com.example.auth_service.repositories.jpa.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "/api/auth", description = "controller to register and authenticate new users")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository repository;
    private final TokenService tokenService;
    private final BlacklistRepository  blacklistRepository;
    private final CacheManager cacheManager;
    private final UserEventProducer userEventProducer;

    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponse(responseCode = "200", description = "User logged in successfully")
    @ApiResponse(responseCode = "404", description = "BadRequestException, user not authenticated")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok().body(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    @Operation(summary = "save new users")
    @ApiResponse(responseCode = "201", description = "New user registered")
    @ApiResponse(responseCode = "400", description = "BadRequestException, user exists")
    @ApiResponse(responseCode = "500", description = "Server error")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO data) {
        if(repository.findByEmail(data.email()) != null) return  ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.name(), data.email(), encryptedPassword, data.role());

        repository.save(newUser);
        userEventProducer.publishUserRegistered(newUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest  request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.replace("Bearer ", "");

        String jti = tokenService.extractJti(token);
        long ttlSeconds = tokenService.extractRemainingTtlMillis(token) / 1000;

        if(jti != null) {
            blacklistRepository.save(new BlacklistToken(jti, ttlSeconds));
        }

        String email = tokenService.validateToken(token);
        if(email != null) {
            cacheManager.getCache("users").evict("user:" + email);
        }

        return ResponseEntity.noContent().build();
    }
}
