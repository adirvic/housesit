package br.com.rodrigues.housesit.controller;

import br.com.rodrigues.housesit.dto.AuthResponse;
import br.com.rodrigues.housesit.dto.DevLoginRequest;
import br.com.rodrigues.housesit.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Passwordless login for local development and demos. It lets anyone sign in as any email,
 * so the bean only exists when {@code app.auth.dev-login.enabled=true} (set by the dev profile).
 * Everywhere else the endpoint is simply not mapped.
 */
@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(name = "app.auth.dev-login.enabled", havingValue = "true")
public class DevAuthController {

    private static final Logger log = LoggerFactory.getLogger(DevAuthController.class);

    private final AuthService authService;

    public DevAuthController(AuthService authService) {
        this.authService = authService;
        log.warn("Dev login endpoint POST /api/auth/dev-login is ENABLED. Never enable it in production.");
    }

    @PostMapping("/dev-login")
    public ResponseEntity<AuthResponse> devLogin(@Valid @RequestBody DevLoginRequest request) {
        AuthResponse response = authService.devLogin(request);
        return ResponseEntity.ok(response);
    }
}
