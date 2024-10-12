package org.bakeryshop.domain.model.pancakes;

public enum PancakeIngredient {

    DARK_CHOCOLATE("dark chocolate"),
    MILK_CHOCOLATE("milk chocolate"),
    WHIPPED_CREAM("whipped cream"),
    HAZELNUTS("hazelnuts");

    private final String displayName;

    PancakeIngredient(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
