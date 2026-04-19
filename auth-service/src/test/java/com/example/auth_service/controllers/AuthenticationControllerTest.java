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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
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

        lenient().when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        lenient().when(tokenService.generateToken(user)).thenReturn("token.gerado");
        lenient().when(repository.findByEmail("joao@email.com")).thenReturn(null);
        lenient().when(tokenService.extractJti("token.valido")).thenReturn("jti-123");
        lenient().when(tokenService.extractRemainingTtlMillis("token.valido")).thenReturn(7200000L);
        lenient().when(tokenService.validateToken("token.valido")).thenReturn("joao@email.com");
        lenient().when(cacheManager.getCache("users")).thenReturn(cache);
        lenient().when(httpRequest.getHeader("Authorization")).thenReturn("Bearer token.valido");
    }

    @Test
    void login_comCredenciaisValidas_deveRetornarToken() {
        var response = controller.login(new AuthenticationDTO("joao@email.com", "123456"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("token.gerado", response.getBody().token());
    }

    @Test
    void register_comEmailNovo_deveSalvarEPublicarEvento() {
        var response = controller.register(
                new RegisterDTO("João", "joao@email.com", "123456", Role.CUSTOMER)
        );

        assertEquals(200, response.getStatusCode().value());
        verify(repository).save(any(User.class));
        verify(userEventProducer).publishUserRegistered(any(User.class));
    }

    @Test
    void register_comEmailExistente_deveRetornar400SemSalvar() {
        when(repository.findByEmail("joao@email.com")).thenReturn(user);

        var response = controller.register(
                new RegisterDTO("João", "joao@email.com", "123456", Role.CUSTOMER)
        );

        assertEquals(400, response.getStatusCode().value());
        verify(repository, never()).save(any());
        verify(userEventProducer, never()).publishUserRegistered(any());
    }

    @Test
    void logout_comTokenValido_deveRevogarELimparCache() {
        var response = controller.logout(httpRequest);

        assertEquals(204, response.getStatusCode().value());
        verify(blacklistRepository).save(any(BlacklistToken.class));
        verify(cache).evict("joao@email.com");
    }

    @Test
    void logout_semAuthorizationHeader_deveRetornar400() {
        when(httpRequest.getHeader("Authorization")).thenReturn(null);

        var response = controller.logout(httpRequest);

        assertEquals(400, response.getStatusCode().value());
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void logout_comHeaderSemBearer_deveRetornar400() {
        when(httpRequest.getHeader("Authorization")).thenReturn("token.sem.bearer");

        var response = controller.logout(httpRequest);

        assertEquals(400, response.getStatusCode().value());
        verifyNoInteractions(blacklistRepository);
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