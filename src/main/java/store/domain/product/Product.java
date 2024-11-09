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

    // 재고 감소
    public void decreaseQuantity(int orderQuantity) {
        validateOrderQuantity(orderQuantity);
        validateStock(orderQuantity);
        this.quantity -= orderQuantity;
    }

    // 주문 수량 검증
    private void validateOrderQuantity(int orderQuantity) {
        if (orderQuantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 0보다 커야 합니다.");
        }
    }

    // 재고가 충분한지 검증
    private void validateStock(int orderQuantity) {
        if (this.quantity < orderQuantity) {
            throw new IllegalStateException(
                    String.format("[%s] 상품의 재고가 부족합니다.", name)
            );
        }
    }

    // 상품 표시
    public String describeProduct(List<Product> allProducts) {
        if (isStockEmpty()) {
            return String.format("- %s %,d원 재고 없음 %s", name, price, formatPromotion().trim());
        }
        if (hasPromotion() && !hasNonPromotionalStock(allProducts)) {
            return String.format("- %s %,d원 %d개 %s\n"
                    + "- %s %,d원 재고 없음", name, price, quantity, formatPromotion().trim(), name, price);
        }
        return String.format("- %s %,d원 %d개 %s", name, price, quantity, formatPromotion().trim());
    }

    private boolean hasNonPromotionalStock(List<Product> allProducts) {
        for (Product product : allProducts) {
            if (product.matchesName(name) && !product.hasPromotion() && product.quantity > 0) {
                return true;
            }
        }
        return false;
    }

    private String formatPromotion() {
        if (hasPromotion()) {
            return " " + promotionName;
        }
        return "";
    }

    // 유요한 프로모션이 있는지 확인
    public boolean hasPromotion() {
        if (promotionName == null || promotionName.isEmpty() || promotionName.equals("null")) {
            return false;
        }
        return true;
    }

    public boolean matchesName(String targetName) {
        return this.name.equals(targetName);
    }

    public int calculateAmountFor(int quantity) {
        return this.price * quantity;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getPromotionName() {
        return promotionName;
    }

    private boolean isStockEmpty() {
        return quantity == 0;
    }

    public boolean hasEnoughStock(int orderQuantity) {
        return quantity >= orderQuantity;
    }
}
