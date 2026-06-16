package com.example.auth_service.services;

import com.example.auth_service.repositories.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {
    private final UserRepository repository;

    @Cacheable(cacheNames = "users", key = "'user:' + #username", unless = "#result == null")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDetails user = repository.findByEmail(email);
        if (user == null) throw new UsernameNotFoundException("User not found: " + email);
        return user;
    }
}
