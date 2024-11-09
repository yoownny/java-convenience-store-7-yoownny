package store.domain.product;

public class Product {
    private final String name;
    private final int price;
    private final String promotionName;
    private int quantity;

    public Product(String name, int price, int quantity, String promotionName) {
        validateProduct(name, price);
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.promotionName = promotionName;
    }

    private void validateProduct(String name, int price) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("상품 가격은 0보다 커야 합니다.");
        }
    }

    public void decreaseQuantity(int orderQuantity) {
        validateOrderQuantity(orderQuantity);
        validateStock(orderQuantity);
        this.quantity -= orderQuantity;
    }

    private void validateOrderQuantity(int orderQuantity) {
        if (orderQuantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 0보다 커야 합니다.");
        }
    }

    private void validateStock(int orderQuantity) {
        if (this.quantity < orderQuantity) {
            throw new IllegalStateException(
                    String.format("[%s] 상품의 재고가 부족합니다.", name)
            );
        }
    }

    public String describeProduct() {
        if (isStockEmpty()) {
            return String.format("- %s %,d원 재고 없음%s", name, price, formatPromotion());
        }
        return String.format("- %s %,d원 %d개%s", name, price, quantity, formatPromotion());
    }

    private String formatPromotion() {
        if (hasPromotion()) {
            return " " + promotionName;
        }
        return "";
    }

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
