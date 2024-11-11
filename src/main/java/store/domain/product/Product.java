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
            throw new IllegalArgumentException("주문 수량은 0보다 커야 합니다.");
        }
    }

    private void validateStock(int orderQuantity) {
        if (this.quantity < orderQuantity) {
            throw new IllegalStateException("상품의 재고가 부족합니다.");
        }
    }

    public String describeProduct(List<Product> allProducts) {
        if (isStockEmpty()) {
            return String.format("- %s %,d원 재고 없음 %s", name, price, formatPromotion().trim());
        }
        if (hasPromotion() && !hasNonPromotionalStock(allProducts)) {
            return String.format("- %s %,d원 %d개 %s\n- %s %,d원 재고 없음", name, price, quantity, formatPromotion().trim(), name, price);
        }
        return String.format("- %s %,d원 %d개 %s", name, price, quantity, formatPromotion().trim());
    }


    private boolean isStockEmpty() {
        return quantity == 0;
    }

    private String formatPromotion() {
        if (hasPromotion()) {
            return " " + promotionName;
        }
        return "";
    }

    public boolean hasPromotion() {
        return promotionName != null && !promotionName.isEmpty() && !promotionName.equals("null");
    }

    private boolean hasNonPromotionalStock(List<Product> allProducts) {
        for (Product product : allProducts) {
            if (product.matchesName(name) && !product.hasPromotion() && product.quantity > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesName(String targetName) {
        return this.name.equals(targetName);
    }

    public boolean hasEnoughStock(int orderQuantity) {
        return quantity >= orderQuantity;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPromotionName() {
        return promotionName;
    }

}
