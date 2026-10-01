package br.com.rodrigues.housesit.dto;

import java.math.BigDecimal;

public record BalanceSummaryDto(
        Long debtorUserId,
        String debtorName,
        Long creditorUserId,
        String creditorName,
        BigDecimal netAmountOwed,
        String summaryMessage,
        boolean isSettled
) {}
