package store.domain.product;

import java.util.List;

public class Product {
    private final String name;
    private final int price;
    private final String promotionName;
    private int quantity;

    public Product(String name, int price, int quantity, String promotionName) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.promotionName = promotionName;
    }

    public void decreaseQuantity(int amount) {
        validateOrderQuantity(amount);
        validateStock(amount);
        this.quantity -= amount;
    }

    private void validateOrderQuantity(int orderQuantity) {
        if (orderQuantity <= 0) {
            throw new IllegalArgumentException("올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요.");
        }
    }

    private void validateStock(int orderQuantity) {
        if (this.quantity < orderQuantity) {
            throw new IllegalStateException("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.");
        }
    }

    public String describeProduct(List<Product> allProducts) {
        if (isStockEmpty()) {
            return formatEmptyStockDescription();
        }
        return formatAvailableStockDescription(allProducts);
    }

    private String formatEmptyStockDescription() {
        return String.format("- %s %,d원 재고 없음 %s", name, price, formatPromotion());
    }

    private String formatAvailableStockDescription(List<Product> allProducts) {
        String baseDescription = String.format("- %s %,d원 %d개 %s", name, price, quantity, formatPromotion());
        if (!hasPromotion()) {
            return baseDescription;
        }
        if (hasNonPromotionalStock(allProducts)) {
            return baseDescription;
        }
        return appendNormalStockDescription(baseDescription);
    }

    private String appendNormalStockDescription(String baseDescription) {
        return String.format("%s\n- %s %,d원 재고 없음", baseDescription, name, price);
    }

    private boolean isStockEmpty() {
        return quantity == 0;
    }

    private String formatPromotion() {
        if (!hasPromotion()) {
            return "";
        }
        return promotionName;
    }

    public boolean hasPromotion() {
        return promotionName != null && !promotionName.isEmpty() && !promotionName.equals("null");
    }

    private boolean hasNonPromotionalStock(List<Product> allProducts) {
        return allProducts.stream()
                .filter(p -> p.matchesName(name))
                .filter(p -> !p.hasPromotion())
                .anyMatch(p -> p.quantity > 0);
    }

    public boolean matchesName(String targetName) {
        return this.name.equals(targetName);
    }

    public boolean hasEnoughStock(int orderQuantity) {
        return quantity >= orderQuantity;
    }

    public int priceValue() {
        return price;
    }

    public int quantityValue() {
        return quantity;
    }

    public String promotionNameValue() {
        return promotionName;
    }
}