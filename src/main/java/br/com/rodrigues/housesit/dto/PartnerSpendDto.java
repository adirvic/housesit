package br.com.rodrigues.housesit.dto;

import java.math.BigDecimal;

public record PartnerSpendDto(
        UserDto user,
        BigDecimal totalPaidOutOfPocket,
        BigDecimal totalShareOfExpenses,
        BigDecimal netContribution
) {}
