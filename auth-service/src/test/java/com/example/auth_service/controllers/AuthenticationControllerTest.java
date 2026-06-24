package com.example.auth_service.controllers;

import com.example.auth_service.controllers.dtos.AuthenticationDTO;
import com.example.auth_service.controllers.dtos.RegisterDTO;
import com.example.auth_service.domain.User;
import com.example.auth_service.domain.enums.Role;
import com.example.auth_service.infra.redis.BlacklistToken;
import com.example.auth_service.infra.security.TokenService;
import com.example.auth_service.kafka.producer.UserEventProducer;
import com.example.auth_service.repositories.jpa.UserRepository;
import com.example.auth_service.repositories.redis.BlacklistRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository repository;
    @Mock private TokenService tokenService;
    @Mock private BlacklistRepository blacklistRepository;
    @Mock private CacheManager cacheManager;
    @Mock private Cache cache;
    @Mock private UserEventProducer userEventProducer;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthenticationController controller;

    private User user;

    @BeforeEach
    void setUp() {
        user = buildUser();
    }

    @Test
    @DisplayName("Should login successfully and return token")
    void login_withValidCredentials_shouldReturnToken() {
        var authInput = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authInput);
        when(tokenService.generateToken(user)).thenReturn("token.gerado");

        var response = controller.login(new AuthenticationDTO("joao@email.com", "123456"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token.gerado", response.getBody().token());
    }

    @Test
    @DisplayName("Should register successfully and publish event when email is unique")
    void register_withNewEmail_shouldSaveAndPublishEvent() {
        when(repository.findByEmail("joao@email.com")).thenReturn(null);

        var response = controller.register(
                new RegisterDTO("João", "joao@email.com", "123456", Role.CUSTOMER)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository, times(1)).save(any(User.class));
        verify(userEventProducer, times(1)).publishUserRegistered(any(User.class));
    }

    @Test
    @DisplayName("Should return 400 bad request without saving when email already exists")
    void register_withExistingEmail_shouldReturn400WithoutSaving() {
        when(repository.findByEmail("joao@email.com")).thenReturn(user);

        var response = controller.register(
                new RegisterDTO("João", "joao@email.com", "123456", Role.CUSTOMER)
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
        verify(userEventProducer, never()).publishUserRegistered(any());
    }

    @Test
    @DisplayName("Should complete logout revoking token and clear user cache safely")
    void logout_withValidToken_shouldRevokeAndEvictCache() {
        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer token.valido");
        when(tokenService.extractJti("token.valido")).thenReturn("jti-123");
        when(tokenService.extractRemainingTtlMillis("token.valido")).thenReturn(7200000L);
        when(tokenService.validateToken("token.valido")).thenReturn("joao@email.com");
        when(cacheManager.getCache("users")).thenReturn(cache);

        var response = controller.logout(httpRequest);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(blacklistRepository, times(1)).save(any(BlacklistToken.class));

        // CORREÇÃO: Alinhado à chave real usada pelo controller contendo o prefixo "user:"
        verify(cache, times(1)).evict("user:joao@email.com");
    }

    @Test
    @DisplayName("Should return 400 bad request when header token is missing")
    void logout_withoutAuthorizationHeader_shouldReturn400() {
        when(httpRequest.getHeader("Authorization")).thenReturn(null);

        var response = controller.logout(httpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(blacklistRepository);
        verifyNoInteractions(cacheManager);
    }

    @Test
    @DisplayName("Should return 400 bad request when authorization token does not follow Bearer prefix")
    void logout_withHeaderWithoutBearer_shouldReturn400() {
        when(httpRequest.getHeader("Authorization")).thenReturn("token.sem.bearer");

        var response = controller.logout(httpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(blacklistRepository);
        verifyNoInteractions(cacheManager);
    }

    // Utilitário
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