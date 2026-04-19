package com.example.auth_service.services;

import com.example.auth_service.domain.User;
import com.example.auth_service.domain.enums.Role;
import com.example.auth_service.repositories.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        lenient().when(repository.findByEmail("joao@email.com")).thenReturn(buildUser());
        lenient().when(repository.findByEmail("naoexiste@email.com")).thenReturn(null);
    }

    @Test
    void loadUserByUsername_comEmailExistente_deveRetornarUserDetails() {
        UserDetails result = authorizationService.loadUserByUsername("joao@email.com");

        assertNotNull(result);
        verify(repository).findByEmail("joao@email.com");
    }

    @Test
    void loadUserByUsername_comEmailInexistente_deveRetornarNull() {
        assertNull(authorizationService.loadUserByUsername("naoexiste@email.com"));
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