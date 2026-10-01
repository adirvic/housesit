package br.com.rodrigues.housesit.domain.model;

public enum ExpenseCategory {
    HOUSING("Housing & Rent", "🏠"),
    GROCERIES("Groceries & Supermarket", "🛒"),
    UTILITIES("Utilities (Water, Power, Internet)", "💡"),
    DINING("Restaurants & Delivery", "🍽️"),
    TRANSPORT("Transport & Fuel", "🚗"),
    HEALTH("Health & Pharmacy", "💊"),
    ENTERTAINMENT("Entertainment & Leisure", "🍿"),
    SHOPPING("Shopping & Personal", "🛍️"),
    TRAVEL("Travel & Vacation", "✈️"),
    EDUCATION("Education & Courses", "📚"),
    PETS("Pets", "🐾"),
    OTHER("Other Expenses", "📦");

    private final String displayName;
    private final String icon;

    ExpenseCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
