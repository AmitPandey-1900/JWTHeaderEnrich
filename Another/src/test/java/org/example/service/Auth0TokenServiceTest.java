import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class Auth0TokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private Auth0TokenService auth0TokenService;

    @BeforeEach
    void setUp() {
        auth0TokenService = new Auth0TokenService(
                restTemplate, 
                objectMapper,
                "src/main/resources/token.json",
                "https://example.auth0.com/oauth/token",
                "your-client-id",
                "your-client-secret",
                "your-audience"
        );
    }

    @Test
    void testGetToken() throws Exception {
        // Mock response from Auth0
        String mockResponse = "{\"access_token\":\"mock-token\",\"expires_in\":3600}";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(mockResponse);
        
        JsonNode mockJsonNode = objectMapper.readTree(mockResponse);

        when(restTemplate.exchange(
                eq("https://example.auth0.com/oauth/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        when(objectMapper.readTree(any(String.class))).thenReturn(mockJsonNode);

        String token = auth0TokenService.getToken();

        assert token.equals("mock-token");
    }
}
