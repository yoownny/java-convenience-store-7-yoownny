package store.domain.promotion;

public enum PromotionType {
    TWO_PLUS_ONE("탄산2+1", 2, 1),
    MD_RECOMMENDATION("MD추천상품", 1, 1),
    FLASH_SALE("반짝할인", 1, 1);

    private final String name;
    private final int buyQuantity;
    private final int gigtQuantity;

    PromotionType(final String name, final int buyQuantity, final int gigtQuantity) {
        this.name = name;
        this.buyQuantity = buyQuantity;
        this.gigtQuantity = gigtQuantity;
    }

}
