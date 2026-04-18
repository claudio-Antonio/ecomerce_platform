package com.example.auth_service.repositories.jpa;

import com.example.auth_service.domain.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Cacheable(value = "users", key = "#email")
    UserDetails findByEmail(String email);

    @CacheEvict(value = "users", key = "#email")
    void deleteByEmail(String email);
}
