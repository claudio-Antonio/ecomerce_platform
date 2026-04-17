package com.example.auth_service.infra.redis;

import com.example.auth_service.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserCacheService {
    private static final String USER_PREFIX = "user:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /* Esse metodo eh usado pelo SecurityFilter para saber quem eh o dono do token sem precisar ir
    * ao bd. -> A: vai ao redis e procura o token atravez da key=email.
    * -> B: Se achar, o dado vem em Json e o objectMapper transforma em objeto Java User.*/
    public Optional<User> getFromCache(String email) {
        Object cached = redisTemplate.opsForValue().get(USER_PREFIX + email); // A
        if(cached == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(cached, User.class)); // B
    }

    /* Esse metodo eh chamado logo apos buscar o user no bd pela primeira vez(salva no cache)*/
    public void cacheUser(User user) {
        redisTemplate.opsForValue().set(
                USER_PREFIX + user.getEmail(),
                user,
                30,
                TimeUnit.MINUTES
        );
    }

    /* Se o user fizer logout do sistema o redis deleta os dados em memoria.*/
    public void evictUser(String email) {
        redisTemplate.delete(USER_PREFIX + email);
    }
}
