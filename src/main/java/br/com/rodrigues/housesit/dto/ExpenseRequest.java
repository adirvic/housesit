package br.com.rodrigues.housesit.dto;

import br.com.rodrigues.housesit.domain.model.ExpenseCategory;
import br.com.rodrigues.housesit.domain.model.SplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Category is required")
        ExpenseCategory category,

        LocalDate expenseDate,

        SplitType splitType,

        BigDecimal customPayerShare,

        BigDecimal customPartnerShare,

        String notes
) {}
