// src/test/java/com/example/service/JwtServiceTest.java
package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetJwtToken() {
        String mockToken = "mockJwtToken";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockToken);
        doNothing().when(redisTemplate).opsForValue().set(anyString(), eq(mockToken), any());

        String jwtToken = jwtService.getJwtToken();
        
        assertEquals(mockToken, jwtToken);
        verify(redisTemplate).opsForValue().set(eq("jwtToken"), eq(mockToken), any());
    }

    @Test
    void testRefreshJwtToken() {
        String mockToken = "mockJwtToken";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockToken);
        doNothing().when(redisTemplate).opsForValue().set(anyString(), eq(mockToken), any());

        String refreshedToken = jwtService.refreshJwtToken();
        
        assertEquals(mockToken, refreshedToken);
        verify(redisTemplate).opsForValue().set(eq("jwtToken"), eq(mockToken), any());
    }
}
