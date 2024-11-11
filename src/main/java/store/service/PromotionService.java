package store.service;

import java.util.List;
import java.util.Map;
import store.FileLoader;
import store.domain.product.Product;
import store.domain.promotion.Promotion;
import store.domain.receipt.ReceiptItem;

public class PromotionService {
    private static final String MD_PROMOTION = "MD추천상품";
    private static final String FLASH_SALE = "반짝할인";
    private static final String CARBONATE_PROMOTION = "탄산2+1";
    private final Map<String, Promotion> promotions;

    public PromotionService() {
        this.promotions = FileLoader.loadPromotions();
    }

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

        if (FLASH_SALE.equals(product.getPromotionName())) {
            return quantity  % 2 == 1 && product.hasEnoughStock(quantity + 1);
        }

        if (CARBONATE_PROMOTION.equals(product.getPromotionName())) {
            int remainingQuantity = quantity % 3;
            return remainingQuantity == 2 && product.hasEnoughStock(quantity + 1);
        }

        return false;
    }

    public Promotion getPromotion(String promotionName) {
        return promotions.get(promotionName);
    }

    public boolean isPromotionProduct(String productName) {
        return productName.contains(MD_PROMOTION)
                || productName.contains(FLASH_SALE)
                || productName.contains(CARBONATE_PROMOTION);
    }

    public boolean isTwoPlusOnePromotion(String promotionName) {
        return CARBONATE_PROMOTION.equals(promotionName);
    }
}
