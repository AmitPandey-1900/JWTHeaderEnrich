import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class Auth0TokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private Auth0TokenService auth0TokenService;

    private static final String TOKEN_FILE_PATH = "/mnt/token/token.json";
    private static final String AUTH_URL = "http://auth0.com/oauth/token";
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String AUDIENCE = "test-audience";

    @BeforeEach
    public void setUp() {
        auth0TokenService = new Auth0TokenService(restTemplate, objectMapper,
                TOKEN_FILE_PATH, AUTH_URL, CLIENT_ID, CLIENT_SECRET, AUDIENCE);
    }

    @Test
    public void testGetToken_FirstTimeFetch() throws IOException {
        // Arrange: No token file exists
        Path path = Paths.get(TOKEN_FILE_PATH);
        if (Files.exists(path)) {
            Files.delete(path);
        }

        String newToken = "new-token";
        long expiresIn = 3600L; // 1 hour

        // Mock the response from Auth0
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("access_token", newToken);
        responseMap.put("expires_in", expiresIn);
        JsonNode responseJsonNode = objectMapper.convertValue(responseMap, JsonNode.class);
        when(restTemplate.exchange(eq(AUTH_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(objectMapper.writeValueAsString(responseMap)));
        when(objectMapper.readTree(any(String.class))).thenReturn(responseJsonNode);

        // Act: Call getToken
        String token = auth0TokenService.getToken();

        // Assert: Token should be fetched from Auth0
        assertNotNull(token);
        assertEquals(newToken, token);
        assertTrue(Files.exists(path)); // Token file should be created

        // Clean up
        Files.delete(path);
    }

    @Test
    public void testGetToken_ExistingValidToken() throws IOException {
        // Arrange: Token file exists with valid token
        Path path = Paths.get(TOKEN_FILE_PATH);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
        }

        String existingToken = "existing-token";
        Instant futureExpiryTime = Instant.now().plusSeconds(3600); // 1 hour from now

        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("access_token", existingToken);
        tokenData.put("expiry_time", futureExpiryTime.toString());
        Files.write(path, objectMapper.writeValueAsBytes(tokenData));
        when(objectMapper.readTree(Files.newBufferedReader(path))).thenReturn(objectMapper.convertValue(tokenData, JsonNode.class));

        // Act: Call getToken
        String token = auth0TokenService.getToken();

        // Assert: Token should be read from disk
        assertNotNull(token);
        assertEquals(existingToken, token);

        // Clean up
        Files.delete(path);
    }

    @Test
    public void testGetToken_ExpiredToken() throws IOException {
        // Arrange: Token file exists with expired token
        Path path = Paths.get(TOKEN_FILE_PATH);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
        }

        String expiredToken = "expired-token";
        Instant pastExpiryTime = Instant.now().minusSeconds(3600); // 1 hour ago

        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("access_token", expiredToken);
        tokenData.put("expiry_time", pastExpiryTime.toString());
        Files.write(path, objectMapper.writeValueAsBytes(tokenData));
        when(objectMapper.readTree(Files.newBufferedReader(path))).thenReturn(objectMapper.convertValue(tokenData, JsonNode.class));

        String newToken = "new-token";
        long expiresIn = 3600L; // 1 hour

        // Mock the response from Auth0
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("access_token", newToken);
        responseMap.put("expires_in", expiresIn);
        JsonNode responseJsonNode = objectMapper.convertValue(responseMap, JsonNode.class);
        when(restTemplate.exchange(eq(AUTH_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(objectMapper.writeValueAsString(responseMap)));
        when(objectMapper.readTree(any(String.class))).thenReturn(responseJsonNode);

        // Act: Call getToken
        String token = auth0TokenService.getToken();

        // Assert: Token should be fetched from Auth0
        assertNotNull(token);
        assertEquals(newToken, token);

        // Clean up
        Files.delete(path);
    }
}
