package store.domain.promotion;

public enum PromotionType {
    TWO_PLUS_ONE("탄산2+1"),
    MD_RECOMMENDATION("MD추천상품"),
    FLASH_SALE("반짝할인");

    private final String name;

    PromotionType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
