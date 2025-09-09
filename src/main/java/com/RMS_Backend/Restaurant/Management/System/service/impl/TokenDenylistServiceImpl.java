package com.RMS_Backend.Restaurant.Management.System.service.impl;



import com.RMS_Backend.Restaurant.Management.System.service.TokenDenylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class TokenDenylistServiceImpl implements TokenDenylistService {

    private final StringRedisTemplate redisTemplate;
    private static final String DENYLIST_PREFIX = "jwt:denylist:";

    /**
     * Adds a token's JTI to the denylist with a specific TTL.
     * @param jti The JTI (JWT ID) of the token to deny.
     * @param ttlInSeconds The time-to-live in seconds, matching the token's remaining validity.
     */
    @Override
    public void addToDenylist(String jti, long ttlInSeconds) {
        String key = DENYLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "denied", ttlInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Checks if a token's JTI is currently in the denylist.
     * @param jti The JTI (JWT ID) to check.
     * @return true if the token is on the denylist, false otherwise.
     */
    @Override
    public boolean isTokenDenied(String jti) {
        String key = DENYLIST_PREFIX + jti;
        return redisTemplate.hasKey(key);
    }

}
