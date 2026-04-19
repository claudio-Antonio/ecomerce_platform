package com.example.auth_service.infra.security;

import com.example.auth_service.domain.User;
import com.example.auth_service.domain.enums.Role;
import com.example.auth_service.repositories.jpa.UserRepository;
import com.example.auth_service.repositories.redis.BlacklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock private TokenService tokenService;
    @Mock private UserRepository userRepository;
    @Mock private BlacklistRepository blacklistRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @BeforeEach
    void setUp() {
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer token.valido");
        lenient().when(tokenService.validateToken("token.valido")).thenReturn("joao@email.com");
        lenient().when(tokenService.extractJti("token.valido")).thenReturn("jti-123");
        lenient().when(blacklistRepository.existsById("jti-123")).thenReturn(false);
        lenient().when(userRepository.findByEmail("joao@email.com")).thenReturn(buildUser());
    }

    @Test
    void doFilter_semToken_deveChamarProximoFiltro() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenService);
    }

    @Test
    void doFilter_comTokenValido_deveAutenticarUsuario() throws Exception {
        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_comTokenNaBlacklist_deveRetornar401() throws Exception {
        when(blacklistRepository.existsById("jti-123")).thenReturn(true);

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_comTokenInvalido_deveChamarProximoFiltroSemAutenticar() throws Exception {
        when(tokenService.validateToken("token.valido")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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