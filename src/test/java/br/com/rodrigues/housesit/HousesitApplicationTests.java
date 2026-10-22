package br.com.rodrigues.housesit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=test-only-jwt-secret-with-at-least-32-bytes")
class HousesitApplicationTests {

    @Test
    void contextLoads() {
    }

}
