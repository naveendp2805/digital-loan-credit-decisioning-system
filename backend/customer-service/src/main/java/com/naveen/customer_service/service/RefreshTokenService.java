package com.naveen.customer_service.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String PREFIX = "refresh:";
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${refresh-token.expiration}")
    private long refreshTokenExpiration;

    public String createRefreshToken() {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public void storeRefreshToken(String refreshToken, String email, String role) {

        String key = PREFIX + hash(refreshToken);

        String value = email + "|" + role;

        redisTemplate.opsForValue().set(key, value, java.time.Duration.ofMillis(refreshTokenExpiration));
    }

    public String getRefreshTokenData(String refreshToken) {

        String key = PREFIX + hash(refreshToken);

        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String refreshToken) {

        String key = PREFIX + hash(refreshToken);

        redisTemplate.delete(key);
    }

    public long getExpirationSeconds() {
        return refreshTokenExpiration / 1000;
    }

    private String hash(String value) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}