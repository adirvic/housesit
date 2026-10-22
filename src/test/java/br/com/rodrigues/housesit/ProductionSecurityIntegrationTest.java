package br.com.rodrigues.housesit;

import br.com.rodrigues.housesit.dto.DevLoginRequest;
import br.com.rodrigues.housesit.dto.GoogleAuthRequest;
import br.com.rodrigues.housesit.security.GoogleTokenVerifierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boots the app without the dev profile, as it would run in production, and checks that
 * none of the development shortcuts are reachable.
 */
@SpringBootTest(properties = {
        "app.jwt.secret=test-only-jwt-secret-with-at-least-32-bytes",
        "app.cors.allowed-origins=https://app.housesit.example"
})
class ProductionSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GoogleTokenVerifierService googleTokenVerifierService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void devLoginEndpointIsNotMapped() throws Exception {
        DevLoginRequest loginReq = new DevLoginRequest("junior@example.com", "Junior", null);

        mockMvc.perform(post("/api/auth/dev-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    void mockGoogleTokensAreRejected() throws Exception {
        assertThat(googleTokenVerifierService.verifyToken("mock-google:junior@example.com:Junior")).isEmpty();

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleAuthRequest("mock-google:junior@example.com:Junior"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void h2ConsoleIsNotPublic() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isForbidden());
    }

    @Test
    void corsRejectsUnknownOrigins() throws Exception {
        mockMvc.perform(options("/api/expenses")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void corsAllowsConfiguredOriginsWithoutCredentials() throws Exception {
        mockMvc.perform(options("/api/expenses")
                        .header(HttpHeaders.ORIGIN, "https://app.housesit.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://app.housesit.example"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }
}
