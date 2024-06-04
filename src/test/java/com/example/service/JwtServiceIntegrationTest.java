// src/test/java/com/example/service/JwtServiceIntegrationTest.java
package com.example.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class JwtServiceIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testGetJwtToken() {
        String mockToken = "mockJwtToken";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockToken);
        doNothing().when(redisTemplate).opsForValue().set(anyString(), eq(mockToken), any());

        String jwtToken = jwtService.getJwtToken();

        assertEquals(mockToken, jwtToken);
        verify(redisTemplate).opsForValue().set(eq("jwtToken"), eq(mockToken), any());
    }
}
