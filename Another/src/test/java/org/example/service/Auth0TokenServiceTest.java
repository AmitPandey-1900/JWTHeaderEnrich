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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class Auth0TokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private Auth0TokenService auth0TokenService;

    private static final String TOKEN_FILE_PATH = "token/token.json";
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String AUDIENCE = "test-audience";

    @BeforeEach
    public void setUp() throws IOException {
        // Initialize ObjectMapper mock behavior
        Path tokenFilePath = getResourceFilePath(TOKEN_FILE_PATH);
        if (!Files.exists(tokenFilePath.getParent())) {
            Files.createDirectories(tokenFilePath.getParent());
        }
        if (!Files.exists(tokenFilePath)) {
            createTokenFile(tokenFilePath);
        }

        // Mock ObjectMapper readValue method
        String tokenJson = new String(Files.readAllBytes(tokenFilePath));
        Map<String, String> tokenData = new ObjectMapper().readValue(tokenJson, Map.class);
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(tokenData);

        // Mock RestTemplate behavior
        ResponseEntity<String> responseEntity = ResponseEntity.ok("{\"access_token\": \"dummy-token\", \"expires_in\": 3600}");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);
    }

    @Test
    public void testGetToken_FirstTimeFetch() throws IOException {
        // Act: Call getToken
        String token = auth0TokenService.getToken();

        // Assert: Token should be fetched (dummy token assumed)
        assertNotNull(token);
        assertTrue(token.startsWith("dummy")); // Dummy token starts with "dummy"
    }

    // Other test methods for different scenarios

    private Path getResourceFilePath(String filePath) throws IOException {
        return Paths.get(ResourceUtils.getURL("classpath:" + filePath).getPath());
    }

    private void createTokenFile(Path filePath) throws IOException {
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("access_token", "dummy-token");
        tokenData.put("expiry_time", Instant.now().plusSeconds(3600).toString()); // 1 hour expiry
        Files.write(filePath, new ObjectMapper().writeValueAsBytes(tokenData));
    }
}
