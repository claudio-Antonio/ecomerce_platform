package com.example.auth_service.infra.redis;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "blacklist")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlacklistToken {
    @Id
    private String jti;
    @TimeToLive
    private Long ttl;
}
