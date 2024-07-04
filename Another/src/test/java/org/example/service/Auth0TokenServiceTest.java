import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = {
        "token.file.path=path/to/token/file",
        "auth0.auth_url=http://mock.auth0.com/oauth/token",
        "auth0.client_id=mockClientId",
        "auth0.client_secret=mockClientSecret",
        "auth0.audience=mockAudience"
})
public class Auth0TokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private Auth0TokenService auth0TokenService;

    @Value("${token.file.path}")
    private String tokenFilePath;

    @Value("${auth0.auth_url}")
    private String authUrl;

    @Value("${auth0.client_id}")
    private String clientId;

    @Value("${auth0.client_secret}")
    private String clientSecret;

    @Value("${auth0.audience}")
    private String audience;

    @BeforeEach
    public void setUp() {
        // Inject environment properties
        ReflectionTestUtils.setField(auth0TokenService, "tokenFilePath", tokenFilePath);
        ReflectionTestUtils.setField(auth0TokenService, "authUrl", authUrl);
        ReflectionTestUtils.setField(auth0TokenService, "clientId", clientId);
        ReflectionTestUtils.setField(auth0TokenService, "clientSecret", clientSecret);
        ReflectionTestUtils.setField(auth0TokenService, "audience", audience);
    }

    @Test
    public void testGetToken() throws Exception {
        // Mock the response from Auth0
        JsonNode responseJson = mock(JsonNode.class);
        when(responseJson.get("access_token")).thenReturn(mock(JsonNode.class));
        when(responseJson.get("access_token").asText()).thenReturn("mockAccessToken");
        when(responseJson.get("expires_in")).thenReturn(mock(JsonNode.class));
        when(responseJson.get("expires_in").asLong()).thenReturn(3600L);

        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn("{\"access_token\":\"mockAccessToken\",\"expires_in\":3600}");

        when(restTemplate.exchange(eq(authUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);
        when(objectMapper.readTree(any(String.class))).thenReturn(responseJson);

        // Test getToken method
        String token = auth0TokenService.getToken();
        assertNotNull(token);
        assertEquals("mockAccessToken", token);

        // Verify interactions
        verify(restTemplate, times(1)).exchange(eq(authUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
        verify(objectMapper, times(1)).readTree(any(String.class));
    }
}
