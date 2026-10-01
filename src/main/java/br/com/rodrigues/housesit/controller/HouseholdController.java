package br.com.rodrigues.housesit.controller;

import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.dto.CreateHouseholdRequest;
import br.com.rodrigues.housesit.dto.HouseholdDto;
import br.com.rodrigues.housesit.dto.JoinHouseholdRequest;
import br.com.rodrigues.housesit.security.UserPrincipal;
import br.com.rodrigues.housesit.service.HouseholdService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/household")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @GetMapping
    public ResponseEntity<HouseholdDto> getHousehold(@AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        HouseholdDto dto = householdService.getHouseholdForUser(user);
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<HouseholdDto> createHousehold(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody CreateHouseholdRequest request) {
        User user = principal.getUser();
        HouseholdDto dto = householdService.createHousehold(user, request);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/join")
    public ResponseEntity<HouseholdDto> joinHousehold(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody JoinHouseholdRequest request) {
        User user = principal.getUser();
        HouseholdDto dto = householdService.joinHousehold(user, request);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/regenerate-invite")
    public ResponseEntity<HouseholdDto> regenerateInvite(@AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        HouseholdDto dto = householdService.regenerateInviteCode(user);
        return ResponseEntity.ok(dto);
    }
}
