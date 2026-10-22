package br.com.rodrigues.housesit.controller;

import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.dto.AuthResponse;
import br.com.rodrigues.housesit.dto.GoogleAuthRequest;
import br.com.rodrigues.housesit.dto.HouseholdDto;
import br.com.rodrigues.housesit.dto.UserDto;
import br.com.rodrigues.housesit.security.UserPrincipal;
import br.com.rodrigues.housesit.service.AuthService;
import br.com.rodrigues.housesit.service.HouseholdService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final HouseholdService householdService;

    public AuthController(AuthService authService, HouseholdService householdService) {
        this.authService = authService;
        this.householdService = householdService;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        AuthResponse response = authService.authenticateWithGoogle(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = principal.getUser();
        HouseholdDto householdDto = householdService.getHouseholdForUser(user);
        return ResponseEntity.ok(Map.of(
                "user", UserDto.fromEntity(user),
                "household", householdDto != null ? householdDto : Map.of()
        ));
    }
}
