package br.com.rodrigues.housesit.controller;

import br.com.rodrigues.housesit.domain.model.ExpenseCategory;
import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.dto.ExpenseRequest;
import br.com.rodrigues.housesit.dto.ExpenseResponse;
import br.com.rodrigues.housesit.security.UserPrincipal;
import br.com.rodrigues.housesit.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) Long payerId) {
        User user = principal.getUser();
        List<ExpenseResponse> expenses = expenseService.getExpenses(user, startDate, endDate, category, payerId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        User user = principal.getUser();
        ExpenseResponse expense = expenseService.getExpenseById(user, id);
        return ResponseEntity.ok(expense);
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExpenseRequest request) {
        User user = principal.getUser();
        ExpenseResponse created = expenseService.createExpense(user, request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        User user = principal.getUser();
        ExpenseResponse updated = expenseService.updateExpense(user, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        User user = principal.getUser();
        expenseService.deleteExpense(user, id);
        return ResponseEntity.noContent().build();
    }
}
