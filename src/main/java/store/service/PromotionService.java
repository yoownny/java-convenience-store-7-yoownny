package store.service;

import camp.nextstep.edu.missionutils.DateTimes;
import java.util.List;
import java.util.Map;
import store.FileLoader;
import store.domain.product.Product;
import store.domain.promotion.Promotion;
import store.domain.promotion.PromotionType;
import store.domain.receipt.ReceiptItem;

public class PromotionService {
    private final Map<String, Promotion> promotions;
    private static final String MD_PROMOTION = "MD추천상품";
    private static final String CARBONATE_PROMOTION = "탄산2+1";

    public PromotionService() {
        this.promotions = FileLoader.loadPromotions();
    }

    // 전체 할인 금액 계산
    public int calculateTotalDiscount(List<ReceiptItem> items) {
        return items.stream()
                .mapToInt(this::calculateItemDiscount)
                .sum();
    }

    private int calculateItemDiscount(ReceiptItem item) {
        if (!item.hasGift()) {
            return 0;
        }

        // 증정 수량만큼의 금액을 할인
        return item.getGiftQuantity() * item.getPrice();
    }

    private Promotion findValidPromotion(String promotionName) {
        Promotion promotion = promotions.get(promotionName);
        if (promotion == null) {
            return null;
        }

        return promotion.isValidOn(DateTimes.now().toLocalDate()) ? promotion : null;
    }

    public boolean canAddMoreItems(String productName, int quantity) {
        Promotion promotion = promotions.get(productName);
        if (promotion == null) {
            return false;
        }

        PromotionType type = PromotionType.fromName(productName);
        return type != null && type.canApplyPromotion(quantity);
    }

    public boolean shouldAskForAdditionalItems(Product product, int quantity) {
        if (!product.hasPromotion() || !product.getPromotionName().equals(MD_PROMOTION)) {
            return false;
        }

        Promotion promotion = promotions.get(product.getPromotionName());
        if (promotion == null || !promotion.isValidOn(DateTimes.now().toLocalDate())) {
            return false;
        }

        return true;
    }

    public int calculateAdditionalQuantity(Product product, int quantity) {
        if (!product.hasPromotion() || !product.getPromotionName().equals(MD_PROMOTION)) {
            return 0;
        }

        return quantity; // MD추천상품은 1+1이므로 구매 수량만큼 추가 가능
    }

    public boolean shouldShowNonPromotionalWarning(Product product, int quantity) {
        if (!product.hasPromotion() || !product.getPromotionName().equals(CARBONATE_PROMOTION)) {
            return false;
        }

        return quantity > calculatePromotionalQuantity(product, quantity);
    }

    public int calculatePromotionalQuantity(Product product, int quantity) {
        if (!product.hasPromotion() || !product.getPromotionName().equals(CARBONATE_PROMOTION)) {
            return quantity;
        }

        // 2+1 프로모션의 경우 3의 배수만큼만 프로모션 적용
        return (quantity / 3) * 3;
    }

    public boolean hasNonPromotionalQuantity(Product product, int quantity) {
        if (!product.hasPromotion() || !product.getPromotionName().equals(CARBONATE_PROMOTION)) {
            return false;
        }

        int promotionalSets = (quantity / 2) * 2;
        return quantity > promotionalSets;
    }

    public int calculateNonPromotionalQuantity(Product product, int quantity) {
        if (!product.hasPromotion() || !product.getPromotionName().equals(CARBONATE_PROMOTION)) {
            return 0;
        }

        return quantity - calculatePromotionalQuantity(product, quantity);
    }


    public int calculateGiftQuantity(Product product, int quantity) {
        if (!product.hasPromotion()) {
            return 0;
        }

        Promotion promotion = promotions.get(product.getPromotionName());
        if (promotion == null || !promotion.isValidOn(DateTimes.now().toLocalDate())) {
            return 0;
        }

        if (product.getPromotionName().equals(CARBONATE_PROMOTION)) {
            // 2+1 프로모션: 2개 구매당 1개 증정
            return quantity / 2;  // 3개 구매시 1개, 6개 구매시 2개 증정
        } else if (product.getPromotionName().equals(MD_PROMOTION)) {
            // MD추천상품: 1+1
            return quantity;
        }

        return 0;
    }

}
