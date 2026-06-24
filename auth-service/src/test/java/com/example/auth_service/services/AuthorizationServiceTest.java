package com.example.auth_service.services;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.auth_service.domain.User;
import com.example.auth_service.domain.enums.Role;
import com.example.auth_service.repositories.jpa.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    @DisplayName("Should return UserDetails when email exists")
    void loadUserByUsername_withExistingEmail_shouldReturnUserDetails() {
        User user = buildUser();
        when(repository.findByEmail("joao@email.com")).thenReturn(user);

        UserDetails result = authorizationService.loadUserByUsername("joao@email.com");

        assertNotNull(result);
        assertThat(result.getUsername()).isEqualTo("joao@email.com");
        verify(repository, times(1)).findByEmail("joao@email.com");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when email does not exist")
    void loadUserByUsername_withNonExistingEmail_shouldThrowUsernameNotFoundException() {
        when(repository.findByEmail("naoexiste@email.com")).thenReturn(null);

        assertThatThrownBy(() -> authorizationService.loadUserByUsername("naoexiste@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: naoexiste@email.com");

        verify(repository, times(1)).findByEmail("naoexiste@email.com");
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