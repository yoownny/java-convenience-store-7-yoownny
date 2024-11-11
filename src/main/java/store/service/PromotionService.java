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
    private static final int TWO_PLUS_ONE_DIVISOR = 3;
    private static final int ONE_PLUS_ONE_DIVISOR = 2;

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
        if (!item.hasGift()) {
            return 0;
        }
        return calculateGiftDiscount(item);
    }

    private int calculateGiftDiscount(ReceiptItem item) {
        return item.giftQuantityValue() * item.priceValue();
    }

    public boolean canAddMoreItems(String productName, Product product, int quantity) {
        if (!isValidForPromotion(product)) {
            return false;
        }
        return checkPromotionAvailability(product, quantity);
    }

    private boolean checkPromotionAvailability(Product product, int quantity) {
        String promotionName = product.promotionNameValue();
        int requiredQuantity = calculatePromotionQuantity(promotionName);
        int remainingQuantity = quantity % requiredQuantity;

        return isValidRemainingQuantity(promotionName, remainingQuantity)
                && product.hasEnoughStock(quantity + 1);
    }

    private boolean isValidForPromotion(Product product) {
        return product != null && product.hasPromotion();
    }

    private int calculatePromotionQuantity(String promotionName) {
        if (isTwoPlusOnePromotion(promotionName)) {
            return TWO_PLUS_ONE_DIVISOR;
        }
        return ONE_PLUS_ONE_DIVISOR;
    }

    private boolean isValidRemainingQuantity(String promotionName, int remainingQuantity) {
        int requiredRemaining = calculateRequiredRemaining(promotionName);
        return remainingQuantity == requiredRemaining;
    }

    private int calculateRequiredRemaining(String promotionName) {
        if (isTwoPlusOnePromotion(promotionName)) {
            return 2;
        }
        return 1;
    }

    public Promotion findPromotion(String promotionName) {
        return promotions.get(promotionName);
    }

    public boolean isPromotionProduct(String productName) {
        return containsAnyPromotion(productName);
    }

    private boolean containsAnyPromotion(String productName) {
        return productName.contains(MD_PROMOTION)
                || productName.contains(FLASH_SALE)
                || productName.contains(CARBONATE_PROMOTION);
    }

    public boolean isTwoPlusOnePromotion(String promotionName) {
        return CARBONATE_PROMOTION.equals(promotionName);
    }
}