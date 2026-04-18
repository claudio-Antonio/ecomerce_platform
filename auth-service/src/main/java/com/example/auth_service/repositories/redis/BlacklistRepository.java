package com.example.auth_service.repositories.redis;

import com.example.auth_service.infra.redis.BlacklistToken;
import org.springframework.data.repository.CrudRepository;

public interface BlacklistRepository extends CrudRepository<BlacklistToken, String> {
}
