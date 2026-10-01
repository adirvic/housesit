package br.com.rodrigues.housesit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        String currency,
        BigDecimal totalHouseholdSpending,
        List<CategorySpendDto> categoryBreakdown,
        List<PartnerSpendDto> partnerBreakdown,
        BalanceSummaryDto balanceSummary,
        List<ExpenseResponse> recentExpenses
) {}
