package br.com.rodrigues.housesit.security;

import br.com.rodrigues.housesit.domain.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-only-jwt-secret-with-at-least-32-bytes";

    @Test
    void refusesToStartWithoutSecret() {
        assertThatThrownBy(() -> new JwtService("", 60_000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void refusesWeakSecret() {
        assertThatThrownBy(() -> new JwtService("too-short", 60_000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void issuesTokensThatOnlyTheSameSecretAccepts() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        JwtService otherService = new JwtService(SECRET + "-rotated", 60_000);
        String token = jwtService.generateToken(new User("ana@example.com", "Ana", null, "sub-ana"));

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("ana@example.com");
        assertThat(otherService.isTokenValid(token)).isFalse();
    }
}
