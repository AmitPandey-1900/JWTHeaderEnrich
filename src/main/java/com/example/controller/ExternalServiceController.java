// src/main/java/com/example/controller/ExternalServiceController.java
package com.example.controller;

import com.example.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ExternalServiceController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private Environment env;

    @GetMapping("/external-service")
    public String callExternalService(@RequestHeader("userId") String userId) {
        String jwtToken = jwtService.getJwtToken();
        String clientId = env.getProperty("client.ids." + userId);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(
                "https://external-service.com/api/data",
                String.class,
                Map.of("Authorization", "Bearer " + jwtToken, "Client-Id", clientId, "User-Id", userId)
        );

        return response;
    }
}
