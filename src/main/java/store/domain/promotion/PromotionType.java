package store.domain.promotion;

public enum PromotionType {
    TWO_PLUS_ONE("탄산2+1", 2, 1),
    MD_RECOMMENDATION("MD추천상품", 1, 1),
    FLASH_SALE("반짝할인", 1, 1);

    private final String name;
    private final int buyQuantity;
    private final int giftQuantity;

    PromotionType(final String name, final int buyQuantity, final int giftQuantity) {
        this.name = name;
        this.buyQuantity = buyQuantity;
        this.giftQuantity = giftQuantity;
    }

    public static PromotionType fromName(String name) {
        for (PromotionType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public boolean canApplyPromotion(int quantity) {
        return quantity >= buyQuantity;
    }

    public String getName() {
        return name;
    }

    public int getGiftQuantity() {
        return giftQuantity;
    }
}
