package br.com.rodrigues.housesit.dto;

import br.com.rodrigues.housesit.domain.model.Expense;
import br.com.rodrigues.housesit.domain.model.ExpenseCategory;
import br.com.rodrigues.housesit.domain.model.SplitType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        String title,
        BigDecimal amount,
        ExpenseCategory category,
        String categoryDisplayName,
        String categoryIcon,
        LocalDate expenseDate,
        SplitType splitType,
        String splitTypeDescription,
        BigDecimal payerShare,
        BigDecimal partnerShare,
        UserDto payer,
        String notes,
        LocalDateTime createdAt
) {
    public static ExpenseResponse fromEntity(Expense expense) {
        if (expense == null) return null;
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getCategory().getDisplayName(),
                expense.getCategory().getIcon(),
                expense.getExpenseDate(),
                expense.getSplitType(),
                expense.getSplitType().getDescription(),
                expense.getPayerShare(),
                expense.getPartnerShare(),
                UserDto.fromEntity(expense.getPayer()),
                expense.getNotes(),
                expense.getCreatedAt()
        );
    }
}
