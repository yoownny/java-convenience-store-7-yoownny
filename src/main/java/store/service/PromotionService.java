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
                .mapToInt(item -> item.getGiftQuantity() * item.getPrice())
                .sum();
    }

    public boolean canAddMoreItems(String productName, int quantity) {
        Promotion promotion = promotions.get(productName);
        if (promotion == null) {
            return false;
        }

        PromotionType type = PromotionType.fromName(productName);
        return type != null && type.canApplyPromotion(quantity);
    }

    public int calculateGiftQuantity(Product product, int quantity) {
        if (!product.hasPromotion()) {
            return 0;
        }

        if (CARBONATE_PROMOTION.equals(product.getPromotionName())) {
            return quantity / 3;  // 3개당 1개 증정
        } else if (MD_PROMOTION.equals(product.getPromotionName())) {
            return quantity;  // 1+1
        }

        return 0;
    }

}
