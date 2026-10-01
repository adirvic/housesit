package br.com.rodrigues.housesit.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Service
public class GoogleTokenVerifierService {

    @Value("${app.google.client-id:}")
    private String configuredClientId;

    private final RestClient restClient;

    public GoogleTokenVerifierService() {
        this.restClient = RestClient.builder().build();
    }

    public record GoogleUserInfo(String sub, String email, String name, String picture, boolean emailVerified) {}

    /**
     * Verifies a Google ID token by calling Google's tokeninfo endpoint.
     * Also supports development mock tokens if configured or formatted as dev tokens.
     */
    public Optional<GoogleUserInfo> verifyToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }

        // Support quick mock/dev token format: "mock-google:<email>:<name>"
        if (idToken.startsWith("mock-google:")) {
            String[] parts = idToken.split(":", 3);
            String email = parts.length > 1 ? parts[1] : "user@example.com";
            String name = parts.length > 2 ? parts[2] : "Google User";
            return Optional.of(new GoogleUserInfo("mock-sub-" + email, email, name, "https://api.dicebear.com/7.x/bottts/svg?seed=" + email, true));
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={id_token}", idToken)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("sub")) {
                return Optional.empty();
            }

            String aud = (String) response.get("aud");
            // If configuredClientId is set and not placeholder, ensure audience matches
            if (configuredClientId != null && !configuredClientId.isBlank()
                    && !configuredClientId.contains("placeholder") && !configuredClientId.equals(aud)) {
                return Optional.empty();
            }

            String sub = (String) response.get("sub");
            String email = (String) response.get("email");
            String name = (String) response.getOrDefault("name", email);
            String picture = (String) response.get("picture");
            boolean emailVerified = Boolean.parseBoolean(String.valueOf(response.getOrDefault("email_verified", "true")));

            return Optional.of(new GoogleUserInfo(sub, email, name, picture, emailVerified));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
