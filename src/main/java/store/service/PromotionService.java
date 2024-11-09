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

    public PromotionService() {
        this.promotions = FileLoader.loadPromotions();
    }

    // 증정 수량 계산
    public int calculateGiftQuantity(Product product, int quantity) {
        if (!product.hasPromotion()) {
            return 0;
        }

        Promotion promotion = findValidPromotion(product.getPromotionName());
        if (promotion == null) {
            return 0;
        }

        return promotion.calculateGiftQuantity(quantity);
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
        Promotion promotion = findValidPromotion(item.getName());
        if (promotion == null) {
            return 0;
        }
        return promotion.calculateDiscount(item.getGiftQuantity(), item.getPrice());
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
}
