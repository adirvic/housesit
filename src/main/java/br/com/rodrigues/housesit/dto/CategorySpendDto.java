package br.com.rodrigues.housesit.dto;

import br.com.rodrigues.housesit.domain.model.ExpenseCategory;

import java.math.BigDecimal;

public record CategorySpendDto(
        ExpenseCategory category,
        String displayName,
        String icon,
        BigDecimal totalAmount,
        double percentage
) {}
