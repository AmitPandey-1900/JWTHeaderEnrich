import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@PropertySource("classpath:resource.properties")
public class Auth0TokenService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String tokenFilePath;
    private final String authUrl;
    private final String clientId;
    private final String clientSecret;
    private final String audience;

    private String token;
    private Instant expiryTime;

    @Autowired
    private Environment env;

    public Auth0TokenService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenFilePath = env.getProperty("token.file.path");
        this.authUrl = env.getProperty("auth0.auth_url");
        this.clientId = env.getProperty("auth0.client_id");
        this.clientSecret = env.getProperty("auth0.client_secret");
        this.audience = env.getProperty("auth0.audience");
        loadTokenFromDisk();
    }

    private void loadTokenFromDisk() {
        try {
            Path path = Paths.get(tokenFilePath);
            if (Files.exists(path)) {
                JsonNode tokenNode = objectMapper.readTree(Files.newBufferedReader(path));
                token = tokenNode.get("access_token").asText();
                expiryTime = Instant.parse(tokenNode.get("expiry_time").asText());
            }
        } catch (IOException e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    private void saveTokenToDisk() {
        try {
            Path path = Paths.get(tokenFilePath);
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("access_token", token);
            tokenData.put("expiry_time", expiryTime.toString());
            Files.write(path, objectMapper.writeValueAsBytes(tokenData), StandardOpenOption.CREATE);
        } catch (IOException e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    private void fetchTokenFromAuth0() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("audience", audience);
        requestBody.put("grant_type", "client_credentials");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(authUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                token = responseJson.get("access_token").asText();
                long expiresIn = responseJson.get("expires_in").asLong();
                expiryTime = Instant.now().plusSeconds(expiresIn - 60); // Subtract 60 seconds to refresh a bit earlier
                saveTokenToDisk();
            } catch (IOException e) {
                // Handle exception
                e.printStackTrace();
            }
        } else {
            // Handle unsuccessful response
            System.err.println("Failed to fetch token: " + response.getStatusCode());
        }
    }

    public String getToken() {
        if (token == null || Instant.now().isAfter(expiryTime)) {
            fetchTokenFromAuth0();
        }
        return token;
    }

    @Scheduled(fixedDelayString = "${token.refresh.delay:300000}")
    private void refreshToken() {
        if (token == null || Instant.now().isAfter(expiryTime.minusSeconds(300))) {
            fetchTokenFromAuth0();
        }
    }
}
