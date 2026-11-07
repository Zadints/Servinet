package org.example.servinet.domain.enums;

public enum StockStatus {

    AVAILABLE("Disponible", "stock-available"),
    LOW_STOCK("Stock bajo", "stock-low"),
    OUT_OF_STOCK("Agotado", "stock-out");

    private final String displayName;
    private final String styleClass;

    StockStatus(String displayName, String styleClass) {
        this.displayName = displayName;
        this.styleClass = styleClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public static StockStatus fromQuantity(
            int quantity,
            int minimumStock
    ) {
        if (quantity <= 0) {
            return OUT_OF_STOCK;
        }

        if (quantity <= minimumStock) {
            return LOW_STOCK;
        }

        return AVAILABLE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}