package br.com.rodrigues.housesit.service;

import br.com.rodrigues.housesit.domain.model.SplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpenseServiceTest {

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(null, null, null);
    }

    @Test
    void testEqualSplit() {
        ExpenseService.SplitCalculation split = expenseService.calculateShares(
                new BigDecimal("100.00"), SplitType.EQUAL, null, null);

        assertEquals(new BigDecimal("50.00"), split.payerShare());
        assertEquals(new BigDecimal("50.00"), split.partnerShare());
    }

    @Test
    void testEqualSplitOddAmount() {
        ExpenseService.SplitCalculation split = expenseService.calculateShares(
                new BigDecimal("99.99"), SplitType.EQUAL, null, null);

        assertEquals(new BigDecimal("50.00"), split.payerShare());
        assertEquals(new BigDecimal("49.99"), split.partnerShare());
        assertEquals(new BigDecimal("99.99"), split.payerShare().add(split.partnerShare()));
    }

    @Test
    void testFullPartnerOwes() {
        ExpenseService.SplitCalculation split = expenseService.calculateShares(
                new BigDecimal("75.50"), SplitType.FULL_PARTNER_OWES, null, null);

        assertEquals(new BigDecimal("0.00"), split.payerShare());
        assertEquals(new BigDecimal("75.50"), split.partnerShare());
    }

    @Test
    void testFullPayerExpense() {
        ExpenseService.SplitCalculation split = expenseService.calculateShares(
                new BigDecimal("120.00"), SplitType.FULL_PAYER_EXPENSE, null, null);

        assertEquals(new BigDecimal("120.00"), split.payerShare());
        assertEquals(new BigDecimal("0.00"), split.partnerShare());
    }

    @Test
    void testPercentageSplit() {
        ExpenseService.SplitCalculation split = expenseService.calculateShares(
                new BigDecimal("200.00"), SplitType.PERCENTAGE, new BigDecimal("60"), null);

        assertEquals(new BigDecimal("120.00"), split.payerShare());
        assertEquals(new BigDecimal("80.00"), split.partnerShare());
    }

    @Test
    void testExactAmountSplit() {
        ExpenseService.SplitCalculation split = expenseService.calculateShares(
                new BigDecimal("150.00"), SplitType.EXACT_AMOUNTS, new BigDecimal("40.00"), null);

        assertEquals(new BigDecimal("40.00"), split.payerShare());
        assertEquals(new BigDecimal("110.00"), split.partnerShare());
    }
}
