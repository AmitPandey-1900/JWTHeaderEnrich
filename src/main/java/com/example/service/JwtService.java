// src/main/java/com/example/service/JwtService.java
package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class JwtService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "jwtToken")
    public String getJwtToken() {
        RestTemplate restTemplate = new RestTemplate();
        String jwtToken = restTemplate.getForObject("https://external-service.com/auth/token", String.class);
        redisTemplate.opsForValue().set("jwtToken", jwtToken, Duration.ofMinutes(50));
        return jwtToken;
    }

    @CachePut(value = "jwtToken")
    @Scheduled(fixedRate = 45 * 60 * 1000)
    public String refreshJwtToken() {
        return getJwtToken();
    }
}
