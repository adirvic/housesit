package br.com.rodrigues.housesit.dto;

import br.com.rodrigues.housesit.domain.model.ExpenseCategory;

public record CategoryDto(
        String name,
        String displayName,
        String icon
) {
    public static CategoryDto fromEnum(ExpenseCategory category) {
        return new CategoryDto(category.name(), category.getDisplayName(), category.getIcon());
    }
}
