package br.com.rodrigues.housesit.controller;

import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.dto.SettlementRequest;
import br.com.rodrigues.housesit.dto.SettlementResponse;
import br.com.rodrigues.housesit.security.UserPrincipal;
import br.com.rodrigues.housesit.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getSettlements(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = principal.getUser();
        List<SettlementResponse> settlements = settlementService.getSettlements(user, startDate, endDate);
        return ResponseEntity.ok(settlements);
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SettlementRequest request) {
        User user = principal.getUser();
        SettlementResponse created = settlementService.recordSettlement(user, request);
        return ResponseEntity.ok(created);
    }
}
