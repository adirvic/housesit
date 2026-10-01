package br.com.rodrigues.housesit.controller;

import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.dto.DashboardSummaryResponse;
import br.com.rodrigues.housesit.security.UserPrincipal;
import br.com.rodrigues.housesit.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        User user = principal.getUser();
        DashboardSummaryResponse response = dashboardService.getMonthlySummary(user, year, month);
        return ResponseEntity.ok(response);
    }
}
