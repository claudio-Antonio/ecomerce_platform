package com.example.order_service.infra.security;

import static org.assertj.core.api.Assertions.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

class TokenServiceTest {

    private TokenService tokenService;
    private String secret = "teste-chave-secreta-order-service-2026";
    private String validToken;
    private String invalidToken = "token.completamente.invalido";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // Injeta a propriedade privada @Value usando o utilitário do Spring Test
        ReflectionTestUtils.setField(tokenService, "secret", secret);

        // Gera um token válido simulando a assinatura do auth-service
        Algorithm algorithm = Algorithm.HMAC256(secret);
        validToken = JWT.create()
                .withIssuer("auth-api")
                .withSubject("usuario@email.com")
                .withClaim("role", "CUSTOMER")
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(algorithm);
    }

    @Test
    @DisplayName("Should return subject when token is valid and issuer matches")
    void validateToken_shouldReturnSubjectWhenTokenIsValid() {
        String subject = tokenService.validateToken(validToken);

        assertThat(subject).isEqualTo("usuario@email.com");
    }

    @Test
    @DisplayName("Should return null when token signature is invalid or altered")
    void validateToken_shouldReturnNullWhenTokenIsInvalid() {
        String subject = tokenService.validateToken(invalidToken);

        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Should return null when token has expired")
    void validateToken_shouldReturnNullWhenTokenIsExpired() {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String expiredToken = JWT.create()
                .withIssuer("auth-api")
                .withSubject("usuario@email.com")
                .withExpiresAt(Instant.now().minusSeconds(10)) // Já expirado
                .sign(algorithm);

        String subject = tokenService.validateToken(expiredToken);

        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Should correctly extract role claim value when token is intact")
    void extractRole_shouldReturnRoleWhenTokenIsValid() {
        String role = tokenService.extractRole(validToken);

        assertThat(role).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("Should return null when trying to extract role from an invalid token")
    void extractRole_shouldReturnNullWhenTokenIsInvalid() {
        String role = tokenService.extractRole(invalidToken);

        assertThat(role).isNull();
    }
}