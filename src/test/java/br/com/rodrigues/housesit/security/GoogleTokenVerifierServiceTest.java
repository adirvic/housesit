package br.com.rodrigues.housesit.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleTokenVerifierServiceTest {

    private static final String CLIENT_ID = "housesit.apps.googleusercontent.com";
    private static final String TOKEN_INFO = "https://oauth2.googleapis.com/tokeninfo?id_token=real-token";

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer google = MockRestServiceServer.bindTo(builder).build();

    @Test
    void acceptsMockTokensOnlyWhenEnabled() {
        GoogleTokenVerifierService verifier = new GoogleTokenVerifierService(builder, CLIENT_ID, true);

        assertThat(verifier.verifyToken("mock-google:ana@example.com:Ana"))
                .hasValueSatisfying(info -> {
                    assertThat(info.email()).isEqualTo("ana@example.com");
                    assertThat(info.name()).isEqualTo("Ana");
                });
    }

    @Test
    void treatsMockTokensAsRealTokensWhenDisabled() {
        google.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=mock-google%3Aana%40example.com%3AAna"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        GoogleTokenVerifierService verifier = new GoogleTokenVerifierService(builder, CLIENT_ID, false);

        assertThat(verifier.verifyToken("mock-google:ana@example.com:Ana")).isEmpty();
        google.verify();
    }

    @Test
    void acceptsTokenIssuedForThisApp() {
        respondWith("""
                {"sub":"123","aud":"%s","email":"ana@example.com","email_verified":"true","name":"Ana"}
                """.formatted(CLIENT_ID));
        GoogleTokenVerifierService verifier = new GoogleTokenVerifierService(builder, CLIENT_ID, false);

        assertThat(verifier.verifyToken("real-token"))
                .hasValueSatisfying(info -> assertThat(info.sub()).isEqualTo("123"));
    }

    @Test
    void rejectsTokenIssuedForAnotherApp() {
        respondWith("""
                {"sub":"123","aud":"some-other-app","email":"ana@example.com","email_verified":"true"}
                """);
        GoogleTokenVerifierService verifier = new GoogleTokenVerifierService(builder, CLIENT_ID, false);

        assertThat(verifier.verifyToken("real-token")).isEmpty();
    }

    @Test
    void rejectsUnverifiedEmail() {
        respondWith("""
                {"sub":"123","aud":"%s","email":"ana@example.com","email_verified":"false"}
                """.formatted(CLIENT_ID));
        GoogleTokenVerifierService verifier = new GoogleTokenVerifierService(builder, CLIENT_ID, false);

        assertThat(verifier.verifyToken("real-token")).isEmpty();
    }

    @Test
    void rejectsAllRealTokensWhenClientIdIsNotConfigured() {
        google.expect(never(), requestTo(TOKEN_INFO));
        GoogleTokenVerifierService verifier = new GoogleTokenVerifierService(builder, "", false);

        assertThat(verifier.verifyToken("real-token")).isEmpty();
        google.verify();
    }

    private void respondWith(String json) {
        google.expect(requestTo(TOKEN_INFO)).andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }
}
