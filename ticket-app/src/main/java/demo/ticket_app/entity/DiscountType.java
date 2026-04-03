package demo.ticket_app.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT;

    @JsonCreator
    public static DiscountType fromValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "PERCENT", "PERCENTAGE" -> PERCENTAGE;
            case "FIXED", "FIXED_AMOUNT", "AMOUNT" -> FIXED_AMOUNT;
            default -> throw new IllegalArgumentException("Unsupported discountType: " + value);
        };
    }
}
