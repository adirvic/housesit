package br.com.rodrigues.housesit.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

@Service
public class GoogleTokenVerifierService {

    static final String MOCK_TOKEN_PREFIX = "mock-google:";
    static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={id_token}";

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierService.class);

    private final RestClient restClient;
    private final String clientId;
    private final boolean mockTokensEnabled;

    @Autowired
    public GoogleTokenVerifierService(ObjectProvider<RestClient.Builder> restClientBuilder,
                                      @Value("${app.google.client-id:}") String clientId,
                                      @Value("${app.auth.mock-google-tokens.enabled:false}") boolean mockTokensEnabled) {
        this(restClientBuilder.getIfAvailable(RestClient::builder), clientId, mockTokensEnabled);
    }

    GoogleTokenVerifierService(RestClient.Builder restClientBuilder, String clientId, boolean mockTokensEnabled) {
        this.restClient = restClientBuilder.build();
        this.clientId = clientId;
        this.mockTokensEnabled = mockTokensEnabled;

        if (mockTokensEnabled) {
            log.warn("Mock Google tokens ('{}<email>:<name>') are ENABLED. Never enable them in production.", MOCK_TOKEN_PREFIX);
        }
        if (!isClientIdConfigured()) {
            log.warn("app.google.client-id is not set: real Google ID tokens will be rejected.");
        }
    }

    public record GoogleUserInfo(String sub, String email, String name, String picture, boolean emailVerified) {}

    /**
     * Verifies a Google ID token by calling Google's tokeninfo endpoint, and only accepts it when
     * it was issued for this app's client ID and carries a verified email.
     * Mock tokens ("mock-google:&lt;email&gt;:&lt;name&gt;") are honoured only when explicitly enabled.
     */
    public Optional<GoogleUserInfo> verifyToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }

        if (mockTokensEnabled && idToken.startsWith(MOCK_TOKEN_PREFIX)) {
            return Optional.of(parseMockToken(idToken));
        }

        // Without a configured audience we cannot tell our tokens from tokens minted for any other app.
        if (!isClientIdConfigured()) {
            return Optional.empty();
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(TOKEN_INFO_URL, idToken)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("sub")) {
                return Optional.empty();
            }

            if (!clientId.equals(response.get("aud"))) {
                return Optional.empty();
            }

            boolean emailVerified = Boolean.parseBoolean(String.valueOf(response.get("email_verified")));
            if (!emailVerified) {
                return Optional.empty();
            }

            String sub = (String) response.get("sub");
            String email = (String) response.get("email");
            String name = (String) response.getOrDefault("name", email);
            String picture = (String) response.get("picture");

            return Optional.of(new GoogleUserInfo(sub, email, name, picture, true));
        } catch (RestClientException e) {
            log.debug("Google ID token verification failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isClientIdConfigured() {
        return clientId != null && !clientId.isBlank();
    }

    private static GoogleUserInfo parseMockToken(String idToken) {
        String[] parts = idToken.split(":", 3);
        String email = parts.length > 1 ? parts[1] : "user@example.com";
        String name = parts.length > 2 ? parts[2] : "Google User";
        return new GoogleUserInfo("mock-sub-" + email, email, name, "https://api.dicebear.com/7.x/bottts/svg?seed=" + email, true);
    }
}
