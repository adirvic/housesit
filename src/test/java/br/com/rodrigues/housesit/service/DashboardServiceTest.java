package br.com.rodrigues.housesit.service;

import br.com.rodrigues.housesit.domain.model.*;
import br.com.rodrigues.housesit.domain.repository.ExpenseRepository;
import br.com.rodrigues.housesit.domain.repository.HouseholdRepository;
import br.com.rodrigues.housesit.domain.repository.SettlementRepository;
import br.com.rodrigues.housesit.dto.DashboardSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private ExpenseRepository expenseRepository;
    private SettlementRepository settlementRepository;
    private HouseholdRepository householdRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        expenseRepository = Mockito.mock(ExpenseRepository.class);
        settlementRepository = Mockito.mock(SettlementRepository.class);
        householdRepository = Mockito.mock(HouseholdRepository.class);
        dashboardService = new DashboardService(expenseRepository, settlementRepository, householdRepository);
    }

    @Test
    void testMonthlySummaryAndCoupleBalanceCalculation() {
        Household household = new Household("Our House", "USD");
        household.setId(1L);

        User husband = new User("husband@example.com", "Junior", null, "sub-1");
        husband.setId(10L);
        husband.setHousehold(household);

        User wife = new User("wife@example.com", "Ana", null, "sub-2");
        wife.setId(20L);
        wife.setHousehold(household);

        household.setMembers(new ArrayList<>(List.of(husband, wife)));

        // Expense 1: Husband pays $100 for Groceries (50/50 split -> wife owes $50)
        Expense exp1 = new Expense(household, husband, "Groceries", new BigDecimal("100.00"),
                ExpenseCategory.GROCERIES, LocalDate.now(), SplitType.EQUAL,
                new BigDecimal("50.00"), new BigDecimal("50.00"), "Supermarket");

        // Expense 2: Wife pays $40 for Dinner (50/50 split -> husband owes $20)
        Expense exp2 = new Expense(household, wife, "Dinner", new BigDecimal("40.00"),
                ExpenseCategory.DINING, LocalDate.now(), SplitType.EQUAL,
                new BigDecimal("20.00"), new BigDecimal("20.00"), "Pizza");

        when(householdRepository.findById(1L)).thenReturn(Optional.of(household));
        when(expenseRepository.findByHouseholdIdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(eq(1L), any(), any()))
                .thenReturn(List.of(exp1, exp2));
        when(settlementRepository.findByHouseholdIdAndSettlementDateBetweenOrderBySettlementDateDescCreatedAtDesc(eq(1L), any(), any()))
                .thenReturn(List.of());

        DashboardSummaryResponse summary = dashboardService.getMonthlySummary(husband, LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        assertNotNull(summary);
        assertEquals(new BigDecimal("140.00"), summary.totalHouseholdSpending());
        assertEquals(2, summary.categoryBreakdown().size());

        // Net balance: Wife owes Husband ($50 - $20 = $30)
        assertFalse(summary.balanceSummary().isSettled());
        assertEquals(wife.getId(), summary.balanceSummary().debtorUserId());
        assertEquals(husband.getId(), summary.balanceSummary().creditorUserId());
        assertEquals(new BigDecimal("30.00"), summary.balanceSummary().netAmountOwed());
        assertTrue(summary.balanceSummary().summaryMessage().contains("Ana owes Junior $30.00"));
    }

    @Test
    void testSettlementBalancesDebt() {
        Household household = new Household("Our House", "USD");
        household.setId(1L);

        User husband = new User("husband@example.com", "Junior", null, "sub-1");
        husband.setId(10L);
        husband.setHousehold(household);

        User wife = new User("wife@example.com", "Ana", null, "sub-2");
        wife.setId(20L);
        wife.setHousehold(household);

        household.setMembers(new ArrayList<>(List.of(husband, wife)));

        // Husband paid $100 (Wife owes $50)
        Expense exp1 = new Expense(household, husband, "Groceries", new BigDecimal("100.00"),
                ExpenseCategory.GROCERIES, LocalDate.now(), SplitType.EQUAL,
                new BigDecimal("50.00"), new BigDecimal("50.00"), "Supermarket");

        // Wife settled $50 to Husband directly
        Settlement settlement = new Settlement(household, wife, husband, new BigDecimal("50.00"), LocalDate.now(), "Bank transfer");

        when(householdRepository.findById(1L)).thenReturn(Optional.of(household));
        when(expenseRepository.findByHouseholdIdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(eq(1L), any(), any()))
                .thenReturn(List.of(exp1));
        when(settlementRepository.findByHouseholdIdAndSettlementDateBetweenOrderBySettlementDateDescCreatedAtDesc(eq(1L), any(), any()))
                .thenReturn(List.of(settlement));

        DashboardSummaryResponse summary = dashboardService.getMonthlySummary(husband, LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        assertTrue(summary.balanceSummary().isSettled());
        assertEquals(new BigDecimal("0.00"), summary.balanceSummary().netAmountOwed());
        assertTrue(summary.balanceSummary().summaryMessage().contains("All settled up"));
    }
}
