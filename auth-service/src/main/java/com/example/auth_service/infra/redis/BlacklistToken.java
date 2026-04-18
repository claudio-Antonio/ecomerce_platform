package com.example.auth_service.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;

    /* Esse metodo eh chamado quando usuario sai(/logout)
     1. verifica se o token nao foi expirado.
     2. comando SET que salva um valor para a chave.
     3. BLACKLIST_PREFIX + jti: O identificador unico do token com prefixo.
     4. revoked precisa estar ali.
     5 e 6. Define que a chave vai durar na blacklist exatamente o tempo restante do token.*/
    public void revokeToken(String jti,   long remainingTtlMillis) {
        if(remainingTtlMillis <= 0) return;

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + jti,
                "revoked",
                remainingTtlMillis,
                TimeUnit.MILLISECONDS
        );
    }
    /* Esse metodo eh chamado pelo SecurityFilter em todas as requisicoes. Basicamente ele pergunta
    * para o redis se o jit daquele token esta na blacklist.*/
    public boolean isRevoked(String jti) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
    }
}
