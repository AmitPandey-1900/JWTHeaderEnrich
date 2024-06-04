// src/test/java/com/example/controller/ExternalServiceControllerTest.java
package com.example.controller;

import com.example.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalServiceControllerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private Environment env;

    @InjectMocks
    private ExternalServiceController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCallExternalService() {
        String userId = "user1";
        String clientId = "client1";
        String jwtToken = "mockJwtToken";
        String mockResponse = "mockResponse";

        when(jwtService.getJwtToken()).thenReturn(jwtToken);
        when(env.getProperty("client.ids." + userId)).thenReturn(clientId);

        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), eq(String.class), anyMap())).thenReturn(mockResponse);

        String response = controller.callExternalService(userId);

        assertEquals(mockResponse, response);
        verify(jwtService).getJwtToken();
        verify(env).getProperty("client.ids." + userId);
    }
}
