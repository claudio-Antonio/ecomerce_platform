package com.example.auth_service.infra.security;

import com.example.auth_service.domain.User;
import com.example.auth_service.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private User user;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        var field = TokenService.class.getDeclaredField("secret");
        field.setAccessible(true);
        field.set(tokenService, "test-secret-key-for-unit-tests");

        user = buildUser();
        token = tokenService.generateToken(user);
    }

    @Test
    void generateToken_deveRetornarTokenValido() {
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_comTokenValido_deveRetornarEmail() {
        assertEquals("joao@email.com", tokenService.validateToken(token));
    }

    @Test
    void validateToken_comTokenInvalido_deveRetornarNull() {
        assertNull(tokenService.validateToken("token.invalido"));
    }

    @Test
    void extractJti_deveRetornarJtiNaoNulo() {
        assertNotNull(tokenService.extractJti(token));
    }

    @Test
    void extractRemainingTtlMillis_deveRetornarValorPositivo() {
        assertTrue(tokenService.extractRemainingTtlMillis(token) > 0);
    }

    @Test
    void extractRemainingTtlMillis_comTokenInvalido_deveRetornarZero() {
        assertEquals(0, tokenService.extractRemainingTtlMillis("token.invalido"));
    }

    // utilitario
    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("João")
                .email("joao@email.com")
                .password("encoded")
                .role(Role.CUSTOMER)
                .build();
    }
}