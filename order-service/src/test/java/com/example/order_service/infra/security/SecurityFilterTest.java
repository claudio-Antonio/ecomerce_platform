package com.example.order_service.infra.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @InjectMocks
    private SecurityFilter securityFilter;

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        // Limpa o contexto de segurança antes de cada teste para evitar poluição
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate user and build authorities list when Bearer token is valid")
    void doFilterInternal_shouldAuthenticateWhenTokenIsValid() throws Exception {
        String rawToken = "valid-token-string";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + rawToken);
        when(tokenService.validateToken(rawToken)).thenReturn("user-id-123");
        when(tokenService.extractRole(rawToken)).thenReturn("ADMIN");

        securityFilter.doFilterInternal(request, response, filterChain);

        // Verifica se o Spring Security agora reconhece o usuário autenticado
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("user-id-123");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");

        // O filtro DEVE sempre chamar o próximo elo da corrente
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication but continue filter chain when Authorization header is completely missing")
    void doFilterInternal_shouldSkipAuthenticationWhenHeaderIsMissing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verifyNoInteractions(tokenService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication but continue filter chain when Bearer token is invalid or malformed")
    void doFilterInternal_shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        String rawToken = "malformed-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + rawToken);
        when(tokenService.validateToken(rawToken)).thenReturn(null); // falhou a validação

        securityFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(tokenService, times(1)).validateToken(rawToken);
        verify(tokenService, never()).extractRole(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}