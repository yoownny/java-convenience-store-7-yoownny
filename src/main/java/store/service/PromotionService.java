package store.service;

import camp.nextstep.edu.missionutils.DateTimes;
import java.util.List;
import java.util.Map;
import store.FileLoader;
import store.domain.product.Product;
import store.domain.promotion.Promotion;
import store.domain.receipt.ReceiptItem;

public class PromotionService {
    private static final String MD_PROMOTION = "MD추천상품";
    private static final String STAR_PROMOTION = "반짝할인";
    private static final String CARBONATE_PROMOTION = "탄산2+1";
    private final Map<String, Promotion> promotions;

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
        if (item.hasGift()) {
            return item.getGiftQuantity() * item.getPrice();
        }
        return 0;
    }

    public boolean canAddMoreItems(String productName, Product product, int quantity) {
        if (!product.hasPromotion()) {
            return false;
        }

        if (MD_PROMOTION.equals(product.getPromotionName())) {
            return quantity  % 2 == 1 && product.hasEnoughStock(quantity + 1);
        }

        if (STAR_PROMOTION.equals(product.getPromotionName())) {
            return quantity  % 2 == 1 && product.hasEnoughStock(quantity + 1);
        }

        if (CARBONATE_PROMOTION.equals(product.getPromotionName())) {
            int remainingQuantity = quantity % 3;
            return remainingQuantity == 2 && product.hasEnoughStock(quantity + 1);
        }

        return false;
    }

    public int calculateGiftQuantity(Product product, int quantity) {
        if (!product.hasPromotion()) {
            return 0;
        }

        // 프로모션 찾기
        Promotion promotion = promotions.get(product.getPromotionName());
        if (promotion == null || !promotion.isValidOn(DateTimes.now().toLocalDate())) {
            return 0;  // 유효하지 않은 프로모션은 증정 없음
        }

        String promotionName = product.getPromotionName();
        if (MD_PROMOTION.equals(promotionName) || STAR_PROMOTION.equals(promotionName)) {
            // 1+1 프로모션: 모든 짝수 개수에 대해 절반 증정
            return quantity / 2;
        }

        if (CARBONATE_PROMOTION.equals(promotionName)) {
            // 2+1 프로모션: 3개 단위로 1개씩 증정
            return quantity / 3;
        }

        return 0;
    }
}
