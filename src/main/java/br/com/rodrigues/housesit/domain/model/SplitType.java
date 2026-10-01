package br.com.rodrigues.housesit.domain.model;

public enum SplitType {
    EQUAL("50% / 50% Equal Split"),
    PERCENTAGE("Custom Percentage Split"),
    EXACT_AMOUNTS("Exact Custom Amounts"),
    FULL_PARTNER_OWES("Paid by User (Partner Owes 100%)"),
    FULL_PAYER_EXPENSE("Personal Expense (Partner Owes 0%)");

    private final String description;

    SplitType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
